package com.security_service.factory;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

@Component
public class CookieFactory {

    public Cookie create(String name, String token, String path, int maxAge) {
        return new Cookie(name, token) {{
            setHttpOnly(true);
            setSecure(false);
            setPath(path);
            setMaxAge(maxAge);
            setAttribute("SameSite", "Strict");
        }};
    }
}
