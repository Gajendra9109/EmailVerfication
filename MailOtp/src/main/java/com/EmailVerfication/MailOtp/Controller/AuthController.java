package com.EmailVerfication.MailOtp.Controller;

import javax.validation.Valid;

import com.EmailVerfication.MailOtp.Dto.LoginRequest;
import com.EmailVerfication.MailOtp.Dto.SignupRequest;
import com.EmailVerfication.MailOtp.Dto.VerifyOtpRequest;
import com.EmailVerfication.MailOtp.MessageOp.ApiResponse;
import com.EmailVerfication.MailOtp.Service.UserService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
	@Autowired
	private UserService userService;
	@Autowired
	private AuthenticationManager authenticationManager;

	@PostMapping("/signup")
	public ResponseEntity<String> signup(@Valid @RequestBody SignupRequest signupRequest) throws Exception {
		if (signupRequest.getFullName() == null || signupRequest.getEmail() == null
				|| signupRequest.getPassword() == null) {
			return new ResponseEntity<>(
					new ApiResponse<String>("E", "Please Enter the name , mail , password", null).toString(),
					HttpStatus.BAD_REQUEST);
		} else if (signupRequest.getPassword() != null && signupRequest.getPassword().length() <= 6) {
			return new ResponseEntity<>(
					new ApiResponse<String>("E", "Please enter the 6 Digit password", null).toString(),
					HttpStatus.LENGTH_REQUIRED);
		}
		userService.signup(signupRequest);
		return new ResponseEntity<>(
				new ApiResponse<String>("S", "Your Verifcation Opt is send to your mailid", signupRequest.getFullName())
						.toString(),
				HttpStatus.OK);

	}

	@PostMapping("/verify-otp")
	public ResponseEntity<String> verifyOtp(@Valid @RequestBody VerifyOtpRequest verifyOtpRequest) {
		try {
			if (verifyOtpRequest.getEmail() == null && verifyOtpRequest.getOtp() == null) {
				return new ResponseEntity<>(new ApiResponse<>("E", "Please Enter EmailId and OTP", null).toString(),
						HttpStatus.BAD_REQUEST);
			} else if (verifyOtpRequest.getOtp() != null && verifyOtpRequest.getOtp().length() != 6) {
				return new ResponseEntity<>(new ApiResponse<>("E", "Please enter a valid 6-digit OTP", null).toString(),
						HttpStatus.LENGTH_REQUIRED);
			}

			String jwtToken = userService.verifyOtpAndGenerateToken(verifyOtpRequest.getEmail(),
					verifyOtpRequest.getOtp());

			return new ResponseEntity<>(
					new ApiResponse<>("S", "Email Verification was Successful", jwtToken).toString(), HttpStatus.OK);

		} catch (Exception e) {
			return new ResponseEntity<>(
					new ApiResponse<>("F", "Email Verification Failed: " + e.getMessage(), null).toString(),
					HttpStatus.BAD_REQUEST);
		}
	}

	@PostMapping("/login")
	public ResponseEntity<String> login(@Valid @RequestBody LoginRequest loginRequest) {
		try {
			if (loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
				return new ResponseEntity<>(
						new ApiResponse<String>("E", "Please Enter the  mail , password", null).toString(),
						HttpStatus.BAD_REQUEST);
			} else if (loginRequest.getPassword() != null && loginRequest.getPassword().length() <= 6) {
				return new ResponseEntity<>(
						new ApiResponse<String>("E", "Please enter the 6 Digit password", null).toString(),
						HttpStatus.LENGTH_REQUIRED);
			}
			Authentication authentication = authenticationManager.authenticate(
					new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
			SecurityContextHolder.getContext().setAuthentication(authentication);

		String jwttoken = userService.authenticateAndGenerateToken(loginRequest.getEmail(),
				loginRequest.getPassword());
			
			return new ResponseEntity<>(
					new ApiResponse<String>("S", "Login Successful", loginRequest.getEmail()).toString() + "TOKEN  "+ jwttoken, HttpStatus.OK);

		} catch (Exception e) {
			return new ResponseEntity<>(new ApiResponse<>("F", "Login Failed", null).toString(),
					HttpStatus.BAD_REQUEST);
		}

	}
	
	
	
}