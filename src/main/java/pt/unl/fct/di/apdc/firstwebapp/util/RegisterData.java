package pt.unl.fct.di.apdc.firstwebapp.util;

public class RegisterData {

	public String username;
	public String pwrd;
	public String name;
	public String email;
	public String phone;
	public String role;
	public String state;

	public RegisterData() {

	}

	public RegisterData(String username, String pwrd, String name, String email, String phone, String role,
			String state) {
		this.pwrd = pwrd;
		this.username = username;
		this.name = name;
		this.email = email;
		this.phone = phone;
		this.role = role;
		this.state = state;
	}

	public boolean isValidName() {
		return (!name.isBlank());
	}

	public boolean isValidUsername() {
		return (!name.isBlank() && username.length() >= 3);
	}

	public boolean isValidPwrd() {
		return (!pwrd.isBlank() && pwrd.length() >= 5);
	}

	public boolean isValidEmail() {
		return (!email.isBlank() && email.contains("@"));
	}
}