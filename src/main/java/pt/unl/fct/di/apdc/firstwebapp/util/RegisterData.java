package pt.unl.fct.di.apdc.firstwebapp.util;

public class RegisterData {

	public String username;
	public String pwd;

	public RegisterData() {

	}

	public RegisterData(String username, String pwd) {
		this.pwd = pwd;
		this.username = username;
	}

	public boolean validRegistration() {
		if (pwd.isBlank() || username.isBlank()) {
			return false;
		}
		return true;
	}
}