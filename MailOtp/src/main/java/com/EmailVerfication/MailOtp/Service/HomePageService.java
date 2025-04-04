package com.EmailVerfication.MailOtp.Service;

import java.util.List;

import com.EmailVerfication.MailOtp.Model.User;
import com.EmailVerfication.MailOtp.Repository.UserRepository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class HomePageService {

	private UserRepository userRepository;
      @Autowired
	public  HomePageService(UserRepository userRepository) {
		this.userRepository = userRepository;
	}

	public List<User> allUsers() {
		System.out.println("Fetching all users...");
		List<User> users1 = userRepository.findAll();
		System.out.println("Users fetched: " + users1);
		return users1;

	}
}