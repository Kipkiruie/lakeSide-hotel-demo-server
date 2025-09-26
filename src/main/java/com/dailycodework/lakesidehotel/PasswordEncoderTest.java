package com.dailycodework.lakesidehotel;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordEncoderTest {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "Admin@123";   // or "passone" if you want
        String hashedPassword = encoder.encode(rawPassword);
        System.out.println("BCrypt hash: " + hashedPassword);
    }
}

