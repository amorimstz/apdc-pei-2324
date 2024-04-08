package pt.unl.fct.di.apdc.firstwebapp.util;

public class ChangePwrdData {

	public String currentPwrd;
	public String newPwrd;
	public String newPwrdConfirm;

	public ChangePwrdData() {

	}

	public ChangePwrdData(String newPwrd, String newPwrdConfirm) {
		this.newPwrd = newPwrd;
		this.newPwrdConfirm = newPwrdConfirm;
	}

	public boolean isValidPwrd() {
		return (!newPwrd.isBlank() && newPwrd.length() >= 5);
	}

	public boolean isValidConfirm() {
		return (newPwrd.equals(newPwrdConfirm));
	}
}
