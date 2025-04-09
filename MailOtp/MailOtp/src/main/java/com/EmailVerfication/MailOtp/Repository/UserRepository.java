package com.EmailVerfication.MailOtp.Repository;

import java.util.Optional;

import com.EmailVerfication.MailOtp.Model.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
	// Find user by email (used for UserDetails lookup and potentially merging
	// accounts)
	Optional<User> findByEmail(String email);

	// Find user by GitHub ID (used for OAuth login)
	Optional<User> findByGithubId(String githubId);
}