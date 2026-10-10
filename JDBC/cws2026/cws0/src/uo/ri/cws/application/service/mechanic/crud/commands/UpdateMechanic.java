package uo.ri.cws.application.service.mechanic.crud.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;

import uo.ri.cws.application.service.mechanic.MechanicCrudService.MechanicDto;
import uo.ri.util.assertion.ArgumentChecks;
import uo.ri.util.exception.BusinessException;
import uo.ri.util.jdbc.Jdbc;

public class UpdateMechanic {

	private static final String UPDATE = "update TMechanics set nif = ?, name = ?, surname = ?, "
			+ "version = version + 1, updatedAt = ? where id = ? and version = ?";
	private static final String EXISTS = "select id from TMechanics where id = ?";
	private static final String NIF_USED_BY_OTHER =
			"select id from TMechanics where nif = ? and id <> ?";

	private MechanicDto dto;

	public UpdateMechanic(MechanicDto dto) {
		ArgumentChecks.isNotNull(dto, "El mecánico no puede ser nulo");
		ArgumentChecks.isNotBlank(dto.id, "El id del mecánico no puede estar vacío");
		ArgumentChecks.isNotBlank(dto.nif, "El nif del mecánico no puede estar vacío");
		ArgumentChecks.isNotBlank(dto.name, "El nombre del mecánico no puede estar vacío");
		ArgumentChecks.isNotBlank(dto.surname, "El apellido del mecánico no puede estar vacío");
		this.dto = dto;
	}

	public void execute() throws BusinessException {

		try (Connection c = Jdbc.createThreadConnection()) {

			checkNifNotUsedByAnother(c);

			try (PreparedStatement pst = c.prepareStatement(UPDATE)) {
				pst.setString(1, dto.nif);
				pst.setString(2, dto.name);
				pst.setString(3, dto.surname);
				pst.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
				pst.setString(5, dto.id);
				pst.setLong(6, dto.version);

				int affectedRows = pst.executeUpdate();

				if (affectedRows == 0) {
					if (!exists(c)) {
						throw new BusinessException(
								"El mecánico que intentas actualizar no existe");
					}
					throw new BusinessException(
							"El mecánico ha sido modificado por otro usuario");
				}
			}

		} catch (SQLException e) {
			throw new RuntimeException("Error en la base de datos al actualizar", e);
		}
	}

	private boolean exists(Connection c) throws SQLException {
		try (PreparedStatement pst = c.prepareStatement(EXISTS)) {
			pst.setString(1, dto.id);
			try (ResultSet rs = pst.executeQuery()) {
				return rs.next();
			}
		}
	}

	private void checkNifNotUsedByAnother(Connection c)
			throws SQLException, BusinessException {

		try (PreparedStatement pst = c.prepareStatement(NIF_USED_BY_OTHER)) {
			pst.setString(1, dto.nif);
			pst.setString(2, dto.id);
			try (ResultSet rs = pst.executeQuery()) {
				if (rs.next()) {
					throw new BusinessException("Ya existe otro mecánico con el nif " + dto.nif);
				}
			}
		}
	}
}