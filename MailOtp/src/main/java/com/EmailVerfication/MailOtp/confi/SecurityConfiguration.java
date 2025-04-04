package com.EmailVerfication.MailOtp.confi;

import java.util.Arrays;

import com.EmailVerfication.MailOtp.Jwt.JwtAuthenticationFilter;
import com.EmailVerfication.MailOtp.Package.OAuth2LoginSuccessHandler;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
@EnableWebSecurity
public class SecurityConfiguration {
	private final AuthenticationProvider authenticationProvider;
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler; // Inject the handler

	public SecurityConfiguration(JwtAuthenticationFilter jwtAuthenticationFilter,
			AuthenticationProvider authenticationProvider, OAuth2LoginSuccessHandler oAuth2LoginSuccessHandler
	) {
		this.authenticationProvider = authenticationProvider;
		this.jwtAuthenticationFilter = jwtAuthenticationFilter;
		this.oAuth2LoginSuccessHandler = oAuth2LoginSuccessHandler; 
	}

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http.csrf(csrf -> csrf.disable()) 
				.cors(cors -> cors.configurationSource(corsConfigurationSource())) 
				.authorizeRequests(authz -> authz.antMatchers( 
						"/api/auth/signup", "/api/auth/login", "/api/auth/verify-otp", "/login**",																	// OAuth callbacks
						"/error", "/oauth2/**"
				).permitAll().antMatchers("/homePage").authenticated() 
					.anyRequest().authenticated() 
				).sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS) 
				).authenticationProvider(authenticationProvider) 
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) // JWT filter
				.oauth2Login(oauth2 -> oauth2 
						.successHandler(oAuth2LoginSuccessHandler) 
				);

		return http.build();
	}

	@Bean
	CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:8080"));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
		configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With", "accept",
				"Origin", "Access-Control-Request-Method", "Access-Control-Request-Headers"));
		configuration.setExposedHeaders(Arrays.asList("Authorization")); 
		configuration.setAllowCredentials(true);
		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration); 
		return source;
	}
}