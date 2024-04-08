package pt.unl.fct.di.apdc.firstwebapp.util;

public class RegisterData {

	public String username;
	public String pwrd;
	public String confirmPwrd;
	public String name;
	public String email;
	public String phone;
	public String address;
	public String postalCode;
	public String occupation;
	public String profileVisibility;
	public String workplace;
	public String NIF;

	public RegisterData() {

	}

	public RegisterData(String username, String pwrd, String confirmPwrd, String name, String email, String phone,
			String address, String occupation, String profileVisibility, String workplace, String postalCode,
			String NIF) {
		this.pwrd = pwrd;
		this.confirmPwrd = confirmPwrd;
		this.username = username;
		this.name = name;
		this.email = email;
		this.phone = phone;
		this.address = address;
		this.postalCode = postalCode;
		this.occupation = occupation;
		this.profileVisibility = profileVisibility;
		this.workplace = workplace;
		this.NIF = NIF;
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
		String regex = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Z|a-z]{2,}$";
		return (!email.isBlank() && email.matches(regex));
	}

	public boolean isValidPhone() {
		String regex = "^[^a-zA-Z]*$";
		return (!phone.isBlank() && phone.matches(regex));
	}

}