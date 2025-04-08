package com.EmailVerfication.MailOtp.Package;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.EmailVerfication.MailOtp.Model.User;
import com.EmailVerfication.MailOtp.Service.JwtService;
import com.EmailVerfication.MailOtp.Service.UserService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

@Component
public class OAuth2LoginSuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private static final Logger log = LoggerFactory.getLogger(OAuth2LoginSuccessHandler.class);

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;
    public OAuth2LoginSuccessHandler() {
      
    }


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("OAuth2 Authentication successful for principal: {}", authentication.getName());

        if (!(authentication.getPrincipal() instanceof OAuth2User)) {
             log.error("Principal is not an OAuth2User: {}", authentication.getPrincipal().getClass());
             super.onAuthenticationSuccess(request, response, authentication);
             return;
        }

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();

        log.info("Attempting to process OAuth2 User. Attributes received from provider: {}", oAuth2User.getAttributes());
    
        try {
            User localUser = userService.processOAuthPostLogin(oAuth2User);
            if (localUser != null) {
                log.info("User processed/retrieved from DB: ID={}, Email={}, UserName={}, GitHubID={}, Name={}",
                    localUser.getId(), localUser.getEmail(), localUser.getUsername(), localUser.getGithubId(), localUser.getFullName());
                log.info("Result of localUser.getUsername() before JWT generation: {}", localUser.getUsername());
            } else {
                log.warn("userService.processOAuthPostLogin returned null");
            }
           
            if (localUser == null || localUser.getUsername() == null) {
                log.error("Failed to process or retrieve local user details after OAuth login. localUser is null or getUsername() returned null.");
                response.sendRedirect("/login?error=oauth_processing_failed");
                return;
            }

        } catch (Exception e) {
            log.error("Error processing OAuth2 login success: {}", e.getMessage(), e);
            response.sendRedirect("/login?error=internal_error");
        }
    }
     
}