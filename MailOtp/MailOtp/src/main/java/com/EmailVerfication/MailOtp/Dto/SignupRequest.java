package com.EmailVerfication.MailOtp.Dto;


import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;


public class SignupRequest {
   @NotBlank
   @Email
   private String email;

   private String fullName;
   
   @NotBlank
   @Size(min = 6)
   private String password;

public String getEmail() {
	return email;
}

public void setEmail(String email) {
	this.email = email;
}

public String getFullName() {
	return fullName;
}

public void setFullName(String fullName) {
	this.fullName = fullName;
}

public String getPassword() {
	return password;
}

public void setPassword(String password) {
	this.password = password;
}
   
   
}