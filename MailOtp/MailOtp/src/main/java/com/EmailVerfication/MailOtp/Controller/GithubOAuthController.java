package com.EmailVerfication.MailOtp.Controller;

import com.EmailVerfication.MailOtp.Service.UserService;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class GithubOAuthController {
	private final UserService userService;

	public GithubOAuthController(UserService userService) {
		this.userService = userService;
	}


	@GetMapping("/homePage")
	public String hometest() {
		return "Gajendra Patidar";
	}

}
