package com.EmailVerfication.MailOtp.Service;

import java.util.Optional;
import java.util.Random;

import com.EmailVerfication.MailOtp.Dto.SignupRequest;
import com.EmailVerfication.MailOtp.Model.User;
import com.EmailVerfication.MailOtp.Repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {
	@Autowired
	private UserRepository userRepository;
	@Autowired
	private PasswordEncoder passwordEncoder;
	@Autowired
	private OtpService otpService;
	@Autowired
	private EmailService emailService;
	@Autowired
	private JwtService jwtService;

	private static final Logger log = LoggerFactory.getLogger(UserService.class);
	@Autowired
	private AuthenticationManager authenticationManager;

	public void signup(SignupRequest signupRequest) throws Exception {
		if (userRepository.findByEmail(signupRequest.getEmail()).isPresent()) {

			String otp = otpService.generateOtp();
			otpService.storeOtp(signupRequest.getEmail(), otp);
			emailService.sendVerificationEmail(signupRequest.getEmail(), otp);
			return;

		}
		User user = new User();
		user.setEmail(signupRequest.getEmail());
		String UserNameBasedOnEmail = signupRequest.getEmail().substring(0, 10);
		Random an = new Random();
		int nu = an.nextInt(900000) + 100000;
		String  ra = UserNameBasedOnEmail + nu;
		// Generate and send OTP
		user.setUserName(ra);
		String otp = otpService.generateOtp();
		otpService.storeOtp(signupRequest.getEmail(), otp);
		emailService.sendVerificationEmail(signupRequest.getEmail(), otp);
	}

	@Transactional
	public String verifyOtpAndGenerateToken(String email, String otp) throws Exception {
		// Retrieve OTP from cache
		String storedOtp = otpService.getOtp(email);
		if (storedOtp == null || !storedOtp.equals(otp)) {
			throw new Exception("Invalid OTP provided.");
		}

		// Retrieve the user by email
		Optional<User> userOptional = userRepository.findByEmail(email);
		if (!userOptional.isPresent()) {
			throw new Exception("User not found for email: " + email);
		}

		User user = userOptional.get();

		// Enable user after successful verification
		user.setEnabled(true);
		userRepository.save(user);

		// Clear OTP after verification
		otpService.clearOtp(email);

		// Generate JWT Token
		String jwt = jwtService.generateToken(user);
		if (jwt == null || jwt.isEmpty()) {
			throw new Exception("Failed to generate JWT token.");
		}

		return jwt;
	}

	public Optional<User> findByEmail(String email) {
		return userRepository.findByEmail(email);
	}

	// login
	public String authenticateAndGenerateToken(String email, String password) throws Exception {
		// Authenticate user credentials
		authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));

		// Retrieve the user by email
		Optional<User> userOptional = userRepository.findByEmail(email);
		if (userOptional.isPresent()) {
			User user = userOptional.get();

			// Check if the user is enabled (verified via OTP or other mechanisms)
			if (!user.isEnabled()) {
				throw new Exception("User is not verified. Please verify your email with the OTP.");
			}

			// Generate JWT token after successful authentication and verification
			return jwtService.generateToken(user);
		} else {
			throw new Exception("User not found");
		}
	}

	// github login with oauth

	@Transactional // Ensure operations are atomic
	public User processOAuthPostLogin(OAuth2User oAuth2User) {
		String githubId = oAuth2User.getAttribute("id") != null ? oAuth2User.getAttribute("id").toString() : null;
		String email = oAuth2User.getAttribute("email"); // May be null
		String name = oAuth2User.getAttribute("name"); // May be null
		String login = oAuth2User.getAttribute("login"); // GitHub username

		if (githubId == null) {
			log.error("GitHub ID is null for OAuth2 user: {}", oAuth2User.getAttributes());
			throw new RuntimeException("Could not extract GitHub ID from OAuth2 user.");
		}

		log.info("Processing OAuth user: githubId={}, email={}, name={}, login={}", githubId, email, name, login);

		Optional<User> existingUserOpt = userRepository.findByGithubId(githubId);

		User user;
		if (existingUserOpt.isPresent()) {
			// User already exists with this GitHub ID, update details if necessary
			user = existingUserOpt.get();
			log.info("Found existing user by GitHub ID: {}", githubId);
			boolean updated = false;
			if (email != null && !email.equals(user.getEmail())) {

				Optional<User> userByEmail = userRepository.findByEmail(email);
				if (userByEmail.isPresent() && !userByEmail.get().getGithubId().equals(githubId)) {
					log.warn(
							"Email {} provided by GitHub user {} is already associated with a different account (ID: {}). Skipping email update.",
							email, githubId, userByEmail.get().getId());
					// Decide how to handle this conflict (e.g., ignore, notify admin, link
					// accounts?)
				} else {
					user.setEmail(email);
					updated = true;
				}
			}
			if (name != null && !name.equals(user.getFullName())) {
				user.setFullName(name);
				updated = true;
			}
			if (login != null && !login.equals(user.getUserName())) {
				user.setUserName(login); // Update GitHub username (login)
				updated = true;
			}
			if (!user.isEnabled()) { // Ensure user is enabled
				user.setEnabled(true);
				updated = true;
			}

			if (updated) {
				log.info("Updating user details for GitHub ID: {}", githubId);
				user = userRepository.save(user);
			}

		} else {
			// New user via GitHub
			log.info("Creating new user for GitHub ID: {}", githubId);

			if (email != null) {
				Optional<User> userByEmail = userRepository.findByEmail(email);
				if (userByEmail.isPresent()) {
					log.info("Found existing user by email {}. Linking GitHub ID {} to this account.", email, githubId);
					user = userByEmail.get();
					user.setGithubId(githubId); // Link GitHub ID
					if (name != null && user.getFullName() == null)
						user.setFullName(name);
					if (login != null && user.getUserName() == null)
						user.setUserName(login);
					user.setEnabled(true); // Ensure enabled
					user = userRepository.save(user);
					return user; // Return the linked user
				}
			}
			User newUser = new User();
			newUser.setGithubId(githubId);
			newUser.setEmail(email);
			newUser.setFullName(name);
			newUser.setUserName(login);
			newUser.setEnabled(true);
			user = userRepository.save(newUser);
			log.info("Saved new user with ID: {} for GitHub ID: {}", user.getId(), githubId);
		}
		return user;
	}

}
