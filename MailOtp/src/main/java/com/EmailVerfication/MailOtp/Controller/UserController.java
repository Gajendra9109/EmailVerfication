package com.EmailVerfication.MailOtp.Controller;

import java.util.List;

import com.EmailVerfication.MailOtp.Model.User;
import com.EmailVerfication.MailOtp.Service.HomePageService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/home")
@RestController
public class UserController {

	private HomePageService homePageService;

	public UserController(HomePageService homePageService) {
		super();
		this.homePageService = homePageService;
	}
           
	@GetMapping("/me")
	public ResponseEntity<User> authenticatedUser() {
		Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
		User currentUser = (User) authentication.getPrincipal();
		return ResponseEntity.ok(currentUser);
	}

	String name  = " hiiii";
	String  age = "22";

	@GetMapping("/alluser")
	public ResponseEntity<?> getAllUsers() {
		List<User> users = homePageService.allUsers();
		if (users.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NO_CONTENT).body("No Users Found");
		}
		return ResponseEntity.ok(users);
	}

}