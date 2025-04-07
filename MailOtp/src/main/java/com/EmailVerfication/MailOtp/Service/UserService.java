package com.EmailVerfication.MailOtp.Service;

import java.util.Optional;
import java.util.Random;

import com.EmailVerfication.MailOtp.Dto.SignupRequest;
import com.EmailVerfication.MailOtp.Model.User;
import com.EmailVerfication.MailOtp.Repository.UserRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
	
	
	
	  @Transactional // Add Transactional for atomicity
		public void signup(SignupRequest signupRequest) throws Exception {
			Optional<User> existingUserOpt = userRepository.findByEmail(signupRequest.getEmail());

	        if (existingUserOpt.isPresent()) {
	            User existingUser = existingUserOpt.get();
	            // Optional: Check if the existing user is already enabled.
	            // If they are enabled, maybe prevent re-signup or inform them they already have an account.
	            // If they are not enabled, resending OTP is reasonable.
	            if (existingUser.isEnabled()) {
	                 throw new Exception("User with this email already exists and is verified.");
	                 // Or return a specific response indicating account exists
	            }

				// If user exists but is not enabled, resend OTP
				String otp = otpService.generateOtp();
				otpService.storeOtp(signupRequest.getEmail(), otp);
				emailService.sendVerificationEmail(signupRequest.getEmail(), otp);
				// No need to save again here unless you update something on the existing user
				return;
			}

			// --- If user DOES NOT exist ---
			User user = new User();
			user.setEmail(signupRequest.getEmail());

	        // --- FIX 2: Set and Hash the Password ---
	        if (signupRequest.getPassword() == null || signupRequest.getPassword().isEmpty()) {
	            throw new IllegalArgumentException("Password cannot be empty during signup.");
	        }
	        user.setPassword(passwordEncoder.encode(signupRequest.getPassword())); // Encode and set

			// Generate a username
			String baseUserName = signupRequest.getEmail().split("@")[0]; // Safer way to get part before @
	        baseUserName = baseUserName.length() > 15 ? baseUserName.substring(0, 15) : baseUserName; // Limit length
			Random an = new Random();
			int nu = an.nextInt(900000) + 100000;
			String generatedUserName = baseUserName + nu;
			user.setUserName(generatedUserName); // Use the correct setter (assuming it's setUserName now)

	        // Set Full Name
	        user.setFullName(signupRequest.getFullName());

	        // Set enabled to false initially - verification will enable it
	        user.setEnabled(false);

			    userRepository.save(user);
	   
			// Generate and send OTP
			String otp = otpService.generateOtp();
			otpService.storeOtp(signupRequest.getEmail(), otp);
			emailService.sendVerificationEmail(signupRequest.getEmail(), otp);
		}

	    // ... Keep your verifyOtpAndGenerateToken method as is - it should work now ...
		@Transactional
		public String verifyOtpAndGenerateToken(String email, String otp) throws Exception {
			// Retrieve OTP from cache
			String storedOtp = otpService.getOtp(email);
			if (storedOtp == null || !storedOtp.equals(otp)) {
				// Optional: Clear the invalid OTP attempt?
	            // otpService.clearOtp(email);
				throw new Exception("Invalid OTP provided.");
			}

			// Retrieve the user by email - THIS SHOULD NOW FIND THE USER
			Optional<User> userOptional = userRepository.findByEmail(email);
			if (!userOptional.isPresent()) {
	            // This case should be much rarer now, but keep the check
				throw new Exception("User not found for email: " + email + ". Please sign up first.");
			}

			User user = userOptional.get();

			// Check if already enabled (idempotency)
	        if (user.isEnabled()) {
	             log.warn("User {} already enabled, proceeding to generate token.", email);
	             // Optionally clear OTP anyway
	             otpService.clearOtp(email);
	             return jwtService.generateToken(user); // Generate token even if already enabled
	        }

			// Enable user after successful verification
			user.setEnabled(true);
			userRepository.save(user); // Save the change

			// Clear OTP after verification
			otpService.clearOtp(email);

			// Generate JWT Token
			String jwt = jwtService.generateToken(user);
			if (jwt == null || jwt.isEmpty()) {
				// This indicates a problem in JwtService or UserDetails implementation
				throw new Exception("Failed to generate JWT token after verification.");
			}
			return jwt;
		}


	    // ... other methods (findByEmail, authenticateAndGenerateToken) ...
	    public Optional<User> findByEmail(String email) {
			return userRepository.findByEmail(email);
		}

	    public String authenticateAndGenerateToken(String email, String password) throws Exception {
			// Authenticate user credentials
	        Authentication authentication;
	        try {
			     authentication = authenticationManager.authenticate(
	                 new UsernamePasswordAuthenticationToken(email, password)
	             );
	             // If authentication is successful, SecurityContextHolder is often updated automatically
	             // depending on configuration, but explicitly setting it is fine too.
	             SecurityContextHolder.getContext().setAuthentication(authentication);
	        } catch (BadCredentialsException e) {
	            throw new Exception("Invalid email or password.");
	        } catch (DisabledException e) {
	            throw new Exception("User account is not enabled. Please verify your email.");
	        } catch (Exception e) {
	            log.error("Authentication failed for user {}: {}", email, e.getMessage());
	            throw new Exception("Authentication failed.");
	        }


			// Retrieve the user by email (safe to assume user exists after successful auth)
			User user = (User) authentication.getPrincipal(); // Get user details from Authentication object

	        // Double-check if enabled (already checked by DisabledException usually, but good practice)
			if (!user.isEnabled()) {
				throw new Exception("User is not verified. Please verify your email with the OTP.");
			}

			// Generate JWT token
			return jwtService.generateToken(user);
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
