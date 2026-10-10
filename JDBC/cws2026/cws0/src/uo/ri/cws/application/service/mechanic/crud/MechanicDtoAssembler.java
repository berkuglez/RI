package uo.ri.cws.application.service.mechanic.crud;

import uo.ri.cws.application.persistence.mechanic.MechanicGateway.MechanicRecord;
import uo.ri.cws.application.service.mechanic.MechanicCrudService.MechanicDto;

public class MechanicDtoAssembler {
	
	public static MechanicRecord toRecord(MechanicDto dto) {
		MechanicRecord record = new MechanicRecord();
		record.nif = dto.nif;
		record.name = dto.name;
		record.surname = dto.surname;
		return record;
		
	}
}
