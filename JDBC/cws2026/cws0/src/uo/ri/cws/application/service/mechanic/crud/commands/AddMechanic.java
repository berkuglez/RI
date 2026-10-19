package uo.ri.cws.application.service.mechanic.crud.commands;

import uo.ri.conf.Factories;
import uo.ri.cws.application.persistence.mechanic.MechanicGateway;
import uo.ri.cws.application.persistence.util.command.Command;
import uo.ri.cws.application.service.mechanic.MechanicCrudService.MechanicDto;
import uo.ri.cws.application.service.mechanic.crud.MechanicDtoAssembler;
import uo.ri.util.assertion.ArgumentChecks;
import uo.ri.util.assertion.BusinessChecks;
import uo.ri.util.exception.BusinessException;

public class AddMechanic implements Command<MechanicDto>{
	private MechanicGateway mg = Factories.persistence.forMechanic();
	
	private MechanicDto dto;

	public AddMechanic(MechanicDto dto) {
		ArgumentChecks.isNotNull(dto, "Los datos del mecánico no pueden ser nulos");
		ArgumentChecks.isNotBlank(dto.nif, "El NIF no puede estar vacío");
		ArgumentChecks.isNotBlank(dto.name, "El nombre no puede estar vacío");
		ArgumentChecks.isNotBlank(dto.surname, "El apellido no puede estar vacío");
		this.dto = dto;
	}

	public MechanicDto execute() throws BusinessException {
		BusinessChecks.doesNotExist(mg.findByNif(dto.nif),
				"Ya existe un mecánico con ese NIF");
		var record = MechanicDtoAssembler.toRecord(dto);
		mg.add(record);
		dto.id = record.id;
		dto.version = record.version;
		return dto;
	}
}
