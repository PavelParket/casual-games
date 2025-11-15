package com.security_service.factory;

import jakarta.servlet.http.Cookie;
import org.springframework.stereotype.Component;

@Component
public class CookieFactory implements Factory<Cookie> {

    @Override
    public Cookie create(Object... args) {
        if (args.length < 4) {
            throw new IllegalArgumentException("Expected arguments: name, token, path, max age");
        }

        String name = (String) args[0];
        String token = (String) args[1];
        String path = (String) args[2];
        int maxAge = (int) args[3];

        return create(name, token, path, maxAge);
    }

    private Cookie create(String name, String token, String path, int maxAge) {
        return new Cookie(name, token) {{
            setHttpOnly(true);
            setSecure(false);
            setPath(path);
            setMaxAge(maxAge);
            setAttribute("SameSite", "Strict");
        }};
    }
}
