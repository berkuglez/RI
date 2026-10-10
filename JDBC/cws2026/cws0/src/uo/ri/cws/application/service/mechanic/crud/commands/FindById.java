package uo.ri.cws.application.service.mechanic.crud.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import uo.ri.cws.application.service.mechanic.MechanicCrudService.MechanicDto;
import uo.ri.util.assertion.ArgumentChecks;
import uo.ri.util.exception.BusinessException;
import uo.ri.util.jdbc.Jdbc;

public class FindById {

    private static final String FINDBYID =
            "select id, nif, name, surname, version from TMechanics where id = ?";

    private final String id;

    public FindById(String id) {
        ArgumentChecks.isNotBlank(id, "The mechanic id cannot be null or blank");
        this.id = id;
    }

    public Optional<MechanicDto> execute() throws BusinessException {
        try (Connection c = Jdbc.createThreadConnection();
             PreparedStatement pst = c.prepareStatement(FINDBYID)) {

            pst.setString(1, id);

            try (ResultSet rs = pst.executeQuery()) {
                if (!rs.next()) {
                    return Optional.empty();
                }
                MechanicDto dto = new MechanicDto();
                dto.id = rs.getString("id");
                dto.nif = rs.getString("nif");
                dto.name = rs.getString("name");
                dto.surname = rs.getString("surname");
                dto.version = rs.getLong("version");
                return Optional.of(dto);
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}