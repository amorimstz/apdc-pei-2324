package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeAttributesData {

	public String changingUsername;
	public String newPhone;
	public String newAddress;
	public String newOccupation;

	public ChangeAttributesData() {

	}

	public ChangeAttributesData(String changingUsername, String newPhone, String newAddress, String newOccupation) {
		this.changingUsername = changingUsername;
		this.newPhone = newPhone;
		this.newAddress = newAddress;
		this.newOccupation = newOccupation;

	}
}
