package com.security_service.service;

import com.security_service.exception.MissingTokenException;
import com.security_service.factory.CookieFactory;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class CookieService {

    private final String COOKIE_NAME = "refresh_token";

    private final int COOKIE_MAX_AGE = 24 * 60 * 60;

    private final CookieFactory factory;

    public void addRefreshToken(HttpServletResponse response, String token) {
        response.addCookie(factory.create(COOKIE_NAME, token, "/", COOKIE_MAX_AGE));

        log.debug("Set refresh token cookie for response");
    }

    public void deleteRefreshToken(HttpServletResponse response) {
        response.addCookie(factory.create(COOKIE_NAME, null, "/", 0));

        log.debug("Deleted refresh token cookie");
    }

    public String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new MissingTokenException("Refresh token not found in cookies");
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(() -> new MissingTokenException("Refresh token not found"));
    }
}
