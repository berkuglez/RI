package uo.ri.cws.application.service.mechanic.crud.commands;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import uo.ri.util.assertion.ArgumentChecks;
import uo.ri.util.exception.BusinessException;
import uo.ri.util.jdbc.Jdbc;

public class DeleteMechanic {

    private static final String TMECHANICS_DELETE = "delete from TMechanics where id = ?";
    private static final String HAS_WORKORDERS = "select id from TWorkOrders where mechanic_id = ?";
    private static final String HAS_INTERVENTIONS = "select id from TInterventions where mechanic_id = ?";

    private String idMechanic;

    public DeleteMechanic(String idMechanic) {
        ArgumentChecks.isNotBlank(idMechanic, "El ID del mecánico no puede ser nulo ni vacío");
        this.idMechanic = idMechanic;
    }

    public void execute() throws BusinessException {

        try (Connection c = Jdbc.createThreadConnection()) {

            checkNotUsed(c, HAS_WORKORDERS, "El mecánico tiene órdenes de trabajo asignadas");
            checkNotUsed(c, HAS_INTERVENTIONS, "El mecánico tiene intervenciones realizadas");

            try (PreparedStatement pst = c.prepareStatement(TMECHANICS_DELETE)) {
                pst.setString(1, idMechanic);

                int affectedRows = pst.executeUpdate();

                if (affectedRows == 0) {
                    throw new BusinessException("No existe ningún mecánico con ese identificador");
                }
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error en la base de datos al borrar el mecánico", e);
        }
    }

    private void checkNotUsed(Connection c, String sql, String message)
            throws SQLException, BusinessException {

        try (PreparedStatement pst = c.prepareStatement(sql)) {
            pst.setString(1, idMechanic);
            try (ResultSet rs = pst.executeQuery()) {
                if (rs.next()) {
                    throw new BusinessException(message);
                }
            }
        }
    }
}