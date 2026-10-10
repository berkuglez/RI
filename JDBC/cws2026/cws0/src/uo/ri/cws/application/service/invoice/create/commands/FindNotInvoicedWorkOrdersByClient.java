package uo.ri.cws.application.service.invoice.create.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import uo.ri.cws.application.service.invoice.InvoicingService.InvoicingWorkOrderDto;
import uo.ri.util.assertion.ArgumentChecks;
import uo.ri.util.exception.BusinessException;
import uo.ri.util.jdbc.Jdbc;

public class FindNotInvoicedWorkOrdersByClient {

	private static final String FIND_NOT_INVOICED =
			"select w.id, w.description, w.date, w.state, w.total_amount "
			+ "from TWorkOrders w, TVehicles v, TClients c "
			+ "where w.vehicle_id = v.id and v.client_id = c.id "
			+ "and w.state = 'APPROVED' and c.nif = ? "
			+ "order by w.date";

	private String nif;

	public FindNotInvoicedWorkOrdersByClient(String nif) {
		ArgumentChecks.isNotBlank(nif, "El nif no puede ser nulo ni vacío");
		this.nif = nif;
	}

	public List<InvoicingWorkOrderDto> execute() throws BusinessException {
		List<InvoicingWorkOrderDto> result = new ArrayList<>();

		try (Connection c = Jdbc.createThreadConnection();
				PreparedStatement pst = c.prepareStatement(FIND_NOT_INVOICED)) {

			pst.setString(1, nif);

			try (ResultSet rs = pst.executeQuery()) {
				while (rs.next()) {
					InvoicingWorkOrderDto dto = new InvoicingWorkOrderDto();
					dto.id = rs.getString("id");
					dto.description = rs.getString("description");
					dto.date = rs.getTimestamp("date").toLocalDateTime();
					dto.state = rs.getString("state");
					dto.amount = rs.getBigDecimal("total_amount");
					result.add(dto);
				}
			}
			return result;

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}