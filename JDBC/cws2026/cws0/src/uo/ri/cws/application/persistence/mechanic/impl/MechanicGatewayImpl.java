package uo.ri.cws.application.persistence.mechanic.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import uo.ri.cws.application.persistence.mechanic.MechanicGateway;
import uo.ri.cws.application.service.mechanic.MechanicCrudService.MechanicDto;
import uo.ri.util.exception.BusinessException;
import uo.ri.util.exception.PersistenceException;
import uo.ri.util.jdbc.Jdbc;
import uo.ri.util.jdbc.Queries;

public class MechanicGatewayImpl implements MechanicGateway{
	
	@Override
	public void add(MechanicRecord t) throws PersistenceException {
		Connection c = Jdbc.getCurrentConnection();
		//importante que no este el connecion dentro del try
			try (PreparedStatement pst= c.prepareStatement(Queries.getSQLSentence("TMECHANICS_ADD"))) {
				pst.setString(1, t.id);
				pst.setString(2, t.nif);
				pst.setString(3, t.name);
				pst.setString(4, t.surname);
				pst.setLong(5, t.version);
				pst.setTimestamp(6, Timestamp.valueOf(t.createdAt));
				pst.setTimestamp(6, Timestamp.valueOf(t.updatedAt));
				pst.setString(8, t.entityState);
				
				pst.executeUpdate();
			} catch (SQLException e) {
				throw new RuntimeException(e);
			}
		
	}

	@Override
	public void remove(String id) throws PersistenceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void update(MechanicRecord t) throws PersistenceException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Optional<MechanicRecord> findById(String id) throws PersistenceException {
		// TODO Auto-generated method stub
		return Optional.empty();
	}

	@Override
	public List<MechanicRecord> findAll() throws PersistenceException {
		// TODO Auto-generated method stub
		return null;
	}
	
}
