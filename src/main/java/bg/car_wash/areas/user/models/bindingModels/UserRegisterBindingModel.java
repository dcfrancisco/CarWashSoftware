package bg.car_wash.areas.user.models.bindingModels;

import javax.validation.constraints.Size;

public class UserRegisterBindingModel {

	private Long id;

	@Size(min = 7, max = 65, message = "Invalid name!")
	private String fullName;

	//TODO add email pattern for validation
	@Size(max = 100, message = "Invalid email address!")
	private String email;

	@Size(min = 4, max = 15, message = "Invalid Password!")
	private String password;

	//TODO validation with password are equals
	private String passwordRepeat;

	public UserRegisterBindingModel() {
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getFullName() {
		return fullName;
	}

	public void setFullName(String fullName) {
		this.fullName = fullName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPasswordRepeat() {
		return passwordRepeat;
	}

	public void setPasswordRepeat(String passwordRepeat) {
		this.passwordRepeat = passwordRepeat;
	}
}
