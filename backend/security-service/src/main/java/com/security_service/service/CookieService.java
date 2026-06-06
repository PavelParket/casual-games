package com.security_service.service;

import com.security_service.exception.MissingRefreshTokenException;
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

    private static final String SLASH = "/";
    private static final String COOKIE_NAME = "refresh_token";
    private static final int COOKIE_MAX_AGE = 24 * 60 * 60;
    private static final int COOKIE_ZERO_AGE = 0;

    private final CookieFactory factory;

    public void addRefreshToken(HttpServletResponse response, String token) {
        response.addCookie(factory.create(COOKIE_NAME, token, SLASH, COOKIE_MAX_AGE));
    }

    public void deleteRefreshToken(HttpServletResponse response) {
        response.addCookie(factory.create(COOKIE_NAME, null, SLASH, COOKIE_ZERO_AGE));
    }

    public String extractRefreshToken(HttpServletRequest request) {
        if (request.getCookies() == null) {
            throw new MissingRefreshTokenException();
        }

        return Arrays.stream(request.getCookies())
                .filter(cookie -> COOKIE_NAME.equals(cookie.getName()))
                .findFirst()
                .map(Cookie::getValue)
                .orElseThrow(MissingRefreshTokenException::new);
    }
}
