package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangeAttributesData {

	public String changingUsername;
	public String newPhone;
	public String newAddress;
	public String newPostalCode;
	public String newOccupation;
	public String newName;
	public String newEmail;
	public String newProfileVisibility;
	public String newWorkplace;
	public String newNIF;

	public ChangeAttributesData() {

	}

	public ChangeAttributesData(String changingUsername, String newPhone, String newAddress, String newPostalCode,
			String newOccupation, String newName, String newEmail, String newProfileVisibility, String newWorkplace,
			String newNIF) {
		this.changingUsername = changingUsername;
		this.newPhone = newPhone;
		this.newAddress = newAddress;
		this.newPostalCode = newPostalCode;
		this.newOccupation = newOccupation;
		this.newName = newName;
		this.newEmail = newEmail;
		this.newProfileVisibility = newProfileVisibility;
		this.newWorkplace = newWorkplace;
		this.newNIF = newNIF;
	}
}
