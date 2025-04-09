package com.EmailVerfication.MailOtp.Service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

@Service
public class OtpService {

    private final Map<String, String> otpCache = new HashMap<>();
    private final Random random = new SecureRandom();

    public String generateOtp() {
        // Generate a 6-digit OTP
        int number = random.nextInt(900000) + 100000;
        return String.valueOf(number);
    }

    public void storeOtp(String email, String otp) {
        otpCache.put(email, otp);
    }

    public String getOtp(String email) {
        return otpCache.get(email);
    }

    public void clearOtp(String email) {
        otpCache.remove(email);
    }
}