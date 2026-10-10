package uo.ri.cws.application.service.mechanic.crud.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import uo.ri.cws.application.service.mechanic.MechanicCrudService.MechanicDto;
import uo.ri.util.assertion.ArgumentChecks;
import uo.ri.util.exception.BusinessException;
import uo.ri.util.jdbc.Jdbc;

public class ListMechanic {

	private static final String FIND_BY_NIF =
			"select id, nif, name, surname, version from TMechanics where nif = ?";

	private final String nif;

	public ListMechanic(String nif) {
		ArgumentChecks.isNotBlank(nif, "El NIF no puede ser nulo ni vacío");
		this.nif = nif;
	}

	public MechanicDto execute() throws BusinessException {
		try (Connection c = Jdbc.createThreadConnection();
				PreparedStatement pst = c.prepareStatement(FIND_BY_NIF)) {

			pst.setString(1, nif);

			try (ResultSet rs = pst.executeQuery()) {
				if (!rs.next()) {
					return null;
				}
				MechanicDto dto = new MechanicDto();
				dto.id = rs.getString("id");
				dto.nif = rs.getString("nif");
				dto.name = rs.getString("name");
				dto.surname = rs.getString("surname");
				dto.version = rs.getLong("version");
				return dto;
			}
		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}