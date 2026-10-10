package uo.ri.cws.application.service.invoice.create.commands;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.UUID;

import uo.ri.cws.application.service.invoice.InvoicingService.InvoiceDto;
import uo.ri.util.assertion.ArgumentChecks;
import uo.ri.util.exception.BusinessException;
import uo.ri.util.jdbc.Jdbc;

public class CreateInvoice {

	private static final String INVOICE_ADD = "insert into TInvoices (id, version, number, date, state, "
			+ "subtotal, total, vatRate, vatAmount, createdAt, updatedAt, entityState) "
			+ "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
	private static final String LAST_NUMBER = "select max(number) from TInvoices";
	private static final String FIND_WORKORDER = "select state, total_amount from TWorkOrders where id = ?";
	private static final String LINK_WORKORDER = "update TWorkOrders set invoice_id = ?, "
			+ "state = 'INVOICED', version = version + 1, updatedAt = ? where id = ?";

	private static final LocalDate VAT_CHANGE = LocalDate.of(2012, 7, 1);

	private List<String> workOrderIds;

	public CreateInvoice(List<String> workOrderIds) {
		ArgumentChecks.isNotNull(workOrderIds, "La lista de órdenes no puede ser nula");
		ArgumentChecks.isTrue(!workOrderIds.isEmpty(), "La lista de órdenes no puede estar vacía");
		for (String id : workOrderIds) {
			ArgumentChecks.isNotBlank(id, "Ningún id de orden puede ser nulo ni vacío");
		}
		// copia sin repetidos, para no facturar dos veces la misma orden
		this.workOrderIds = new ArrayList<>(new LinkedHashSet<>(workOrderIds));
	}

	public InvoiceDto execute() throws BusinessException {

		try (Connection c = Jdbc.createThreadConnection()) {
			c.setAutoCommit(false);
			try {
				InvoiceDto invoice = createInvoice(c);
				c.commit();
				return invoice;

			} catch (BusinessException | RuntimeException | SQLException e) {
				c.rollback();
				throw e;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}

	private InvoiceDto createInvoice(Connection c) throws SQLException, BusinessException {

		BigDecimal subtotal = checkAndSumWorkOrders(c);

		InvoiceDto invoice = new InvoiceDto();
		invoice.id = UUID.randomUUID().toString();
		invoice.version = 1L;
		invoice.number = nextInvoiceNumber(c);
		invoice.date = LocalDate.now();
		invoice.state = "ISSUED";
		invoice.vatPercentage = vatPercentage(invoice.date);
		invoice.subtotal = subtotal;
		invoice.vatAmount = subtotal.multiply(invoice.vatPercentage)
				.divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_EVEN);
		invoice.total = subtotal.add(invoice.vatAmount);

		insert(c, invoice);
		linkWorkOrders(c, invoice.id);
		return invoice;
	}

	/* Comprueba que cada orden existe y está APPROVED; devuelve la suma de importes */
	private BigDecimal checkAndSumWorkOrders(Connection c) throws SQLException, BusinessException {
		BigDecimal total = BigDecimal.ZERO;

		try (PreparedStatement pst = c.prepareStatement(FIND_WORKORDER)) {
			for (String id : workOrderIds) {
				pst.setString(1, id);
				try (ResultSet rs = pst.executeQuery()) {
					if (!rs.next()) {
						throw new BusinessException("La orden " + id + " no existe");
					}
					if (!"APPROVED".equals(rs.getString("state"))) {
						throw new BusinessException("La orden " + id + " no está aprobada");
					}
					total = total.add(rs.getBigDecimal("total_amount"));
				}
			}
		}
		return total.setScale(2, RoundingMode.HALF_EVEN);
	}

	private long nextInvoiceNumber(Connection c) throws SQLException {
		try (PreparedStatement pst = c.prepareStatement(LAST_NUMBER);
				ResultSet rs = pst.executeQuery()) {
			return rs.next() ? rs.getLong(1) + 1 : 1L;
		}
	}

	private BigDecimal vatPercentage(LocalDate date) {
		return VAT_CHANGE.isBefore(date) ? BigDecimal.valueOf(21) : BigDecimal.valueOf(18);
	}

	private void insert(Connection c, InvoiceDto inv) throws SQLException {
		Timestamp now = Timestamp.valueOf(LocalDateTime.now());

		try (PreparedStatement pst = c.prepareStatement(INVOICE_ADD)) {
			pst.setString(1, inv.id);
			pst.setLong(2, inv.version);
			pst.setLong(3, inv.number);
			pst.setDate(4, Date.valueOf(inv.date));
			pst.setString(5, inv.state);
			pst.setBigDecimal(6, inv.subtotal);
			pst.setBigDecimal(7, inv.total);
			pst.setBigDecimal(8, inv.vatPercentage);
			pst.setBigDecimal(9, inv.vatAmount);
			pst.setTimestamp(10, now);
			pst.setTimestamp(11, now);
			pst.setString(12, "ENABLED");
			pst.executeUpdate();
		}
	}

	/* Enlaza las órdenes con la factura y las pasa a INVOICED */
	private void linkWorkOrders(Connection c, String invoiceId) throws SQLException {
		Timestamp now = Timestamp.valueOf(LocalDateTime.now());

		try (PreparedStatement pst = c.prepareStatement(LINK_WORKORDER)) {
			for (String id : workOrderIds) {
				pst.setString(1, invoiceId);
				pst.setTimestamp(2, now);
				pst.setString(3, id);
				pst.executeUpdate();
			}
		}
	}
}