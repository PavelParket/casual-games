package com.security_service.service;

import com.common_utils.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import static com.security_service.config.ResourceMessageConstants.REQUIRED_PASSWORD;

@Service
@RequiredArgsConstructor
public class PasswordService {

    private final PasswordEncoder passwordEncoder;

    public String encode(String password) {
        if (password == null || password.isBlank()) {
            throw new BadRequestException(REQUIRED_PASSWORD);
        }

        return passwordEncoder.encode(password);
    }
}
