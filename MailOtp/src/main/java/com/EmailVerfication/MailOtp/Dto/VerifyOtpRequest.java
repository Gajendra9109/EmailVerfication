package com.EmailVerfication.MailOtp.Dto;


import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyOtpRequest {
   @NotBlank
   @Email
   private String email;

   @NotBlank
   private String otp;

public String getEmail() {
	return email;
}

public void setEmail(String email) {
	this.email = email;
}

public String getOtp() {
	return otp;
}

public void setOtp(String otp) {
	this.otp = otp;
}
   
   
   
}