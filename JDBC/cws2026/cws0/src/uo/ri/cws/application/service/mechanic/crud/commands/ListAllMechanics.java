package uo.ri.cws.application.service.mechanic.crud.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import uo.ri.cws.application.service.mechanic.MechanicCrudService.MechanicDto;
import uo.ri.util.exception.BusinessException;
import uo.ri.util.jdbc.Jdbc;

public class ListAllMechanics {

	private static final String FIND_ALL =
			"select id, nif, name, surname, version from TMechanics";

	public List<MechanicDto> execute() throws BusinessException {
		List<MechanicDto> mechanics = new ArrayList<>();

		try (Connection c = Jdbc.createThreadConnection();
				PreparedStatement pst = c.prepareStatement(FIND_ALL);
				ResultSet rs = pst.executeQuery()) {

			while (rs.next()) {
				MechanicDto dto = new MechanicDto();
				dto.id = rs.getString("id");
				dto.nif = rs.getString("nif");
				dto.name = rs.getString("name");
				dto.surname = rs.getString("surname");
				dto.version = rs.getLong("version");
				mechanics.add(dto);
			}
			return mechanics;

		} catch (SQLException e) {
			throw new RuntimeException(e);
		}
	}
}