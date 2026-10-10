package uo.ri.cws.application.persistence.mechanic;

import uo.ri.cws.application.persistence.Gateway;

public interface MechanicGateway extends Gateway<MechanicGateway.MechanicRecord>{
	public class MechanicRecord extends Gateway.BaseRecord{

		public String nif;
		public String name;
		public String surname;
	}
}
