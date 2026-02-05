package com.security_starter.whitelist;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ServiceWhitelistChecker {

    private final ServiceWhitelistProperties whitelistProperties;

    /**
     * Проверяет, является ли источник запроса доверенным сервисом.
     *
     * @param request HTTP запрос
     * @return true если запрос от доверенного сервиса и должен быть пропущен без JWT
     */
    public boolean isWhitelistedService(HttpServletRequest request) {
        if (!whitelistProperties.isEnabled()) {
            return false;
        }

        List<String> whitelistedHosts = whitelistProperties.getHosts();
        if (whitelistedHosts == null || whitelistedHosts.isEmpty()) {
            return false;
        }

        String remoteHost = getRemoteHost(request);
        String remoteAddr = request.getRemoteAddr();

        for (String whitelistedHost : whitelistedHosts) {
            if (matches(remoteHost, whitelistedHost) || matches(remoteAddr, whitelistedHost)) {
                return true;
            }
        }

        return false;
    }

    /**
     * Получает remote host с fallback на IP.
     */
    private String getRemoteHost(HttpServletRequest request) {
        String host = request.getRemoteHost();
        return host != null ? host : request.getRemoteAddr();
    }

    /**
     * Проверяет совпадение хоста с whitelist паттерном.
     * Поддерживает wildcard (*) в конце паттерна.
     *
     * @param host    проверяемый хост
     * @param pattern паттерн из whitelist
     * @return true если совпадает
     */
    private boolean matches(String host, String pattern) {
        if (host == null || pattern == null) {
            return false;
        }

        // Точное совпадение
        if (host.equals(pattern)) {
            return true;
        }

        // Wildcard совпадение (например: *.svc.cluster.local)
        if (pattern.startsWith("*.")) {
            String suffix = pattern.substring(1); // убираем *
            return host.endsWith(suffix);
        }

        // Wildcard в конце (например: 192.168.1.*)
        if (pattern.endsWith(".*")) {
            String prefix = pattern.substring(0, pattern.length() - 1); // убираем *
            return host.startsWith(prefix);
        }

        return false;
    }
}
