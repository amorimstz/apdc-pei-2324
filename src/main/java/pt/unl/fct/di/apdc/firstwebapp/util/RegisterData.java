package pt.unl.fct.di.apdc.firstwebapp.util;

public class RegisterData {

	public String username;
	public String pwd;
	public String name;
	public String email;
	public String phone;

	public RegisterData() {

	}

	public RegisterData(String username, String pwd, String name, String email, String phone) {
		this.pwd = pwd;
		this.username = username;
		this.name = name;
		this.email = email;
		this.phone = phone;
	}

	public boolean isValidName() {
		return (!name.isBlank());
	}

	public boolean isValidUsername() {
		return (!name.isBlank() && username.length() >= 3);
	}

	public boolean isValidPwd() {
		return (!pwd.isBlank() && pwd.length() >= 5);
	}

	public boolean isValidEmail() {
		return (!email.isBlank() && email.contains("@"));
	}
}