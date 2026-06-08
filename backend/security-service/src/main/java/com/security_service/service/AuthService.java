package com.security_service.service;

import com.security_service.domain.dto.AuthResponse;
import com.security_service.domain.dto.LoginRequest;
import com.security_service.domain.dto.RegisterRequest;
import com.security_service.domain.dto.UserResponse;
import com.security_service.domain.dto.WsTicketRequest;
import com.security_service.domain.dto.WsTicketResponse;
import com.security_service.mapper.AuthMapper;
import com.security_service.validator.RefreshTokenValidator;
import com.security_starter.config.AuthenticationToken;
import com.security_starter.enums.Status;
import com.security_starter.helper.PermissionContextHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UserService userService;

    private final TokenService tokenService;

    private final CookieService cookieService;

    private final SessionService sessionService;

    private final WsTicketService wsTicketService;

    private final RefreshTokenValidator refreshTokenValidator;

    private final AuthMapper mapper;

    private final AuthenticationManager authenticationManager;

    private final PermissionContextHelper permissionContextHelper;

    public AuthResponse register(RegisterRequest request, HttpServletResponse response) {
        UserResponse user = userService.create(request);

        return generateTokens(user, response);
    }

    public AuthResponse login(LoginRequest request, HttpServletResponse response) {
        authenticate(request.email(), request.password());

        UserResponse user = userService.getByEmail(request.email());

        return generateTokens(user, response);
    }

    public AuthResponse refresh(HttpServletRequest request, HttpServletResponse response) {
        String oldRefreshToken = cookieService.extractRefreshToken(request);

        RefreshTokenValidator.RefreshClaims claims = refreshTokenValidator.validate(oldRefreshToken);

        UUID guid = claims.guid();
        UUID sid = claims.sid();

        UserResponse user = userService.getByGuid(guid);

        String newRefreshToken = tokenService.generateRefreshToken(guid, sid);
        String newAccessToken = tokenService.generateAccessToken(
                guid,
                user.getEmail(),
                List.of(user.getRole().toString()),
                Status.DEFAULT,
                sid
        );

        sessionService.rotate(guid, sid, oldRefreshToken, newRefreshToken);

        cookieService.addRefreshToken(response, newRefreshToken);

        return mapper.toResponse(user, newAccessToken);
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        try {
            String refreshToken = cookieService.extractRefreshToken(request);
            UUID guid = tokenService.extractGuid(refreshToken);
            UUID sid = tokenService.extractSid(refreshToken);
            sessionService.revoke(guid, sid);
        } catch (Exception e) {
            log.debug("Logout: session revoke skipped: {}", e.getMessage());
        }

        cookieService.deleteRefreshToken(response);
    }

    public WsTicketResponse createWsTicket(WsTicketRequest ticketRequest) {
        AuthenticationToken token = permissionContextHelper.getCurrentAuthentication();

        return WsTicketResponse.builder()
                .ticketId(
                        wsTicketService.create(token.getGuid(), token.getSid(), ticketRequest.roomId())
                )
                .build();
    }

    private void authenticate(String email, String password) {
        authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    private AuthResponse generateTokens(UserResponse user, HttpServletResponse response) {
        UUID sid = UUID.randomUUID();

        String accessToken = tokenService.generateAccessToken(
                user.getGuid(),
                user.getEmail(),
                List.of(user.getRole().toString()),
                Status.DEFAULT,
                sid
        );

        String refreshToken = tokenService.generateRefreshToken(user.getGuid(), sid);

        cookieService.addRefreshToken(response, refreshToken);
        sessionService.createSession(user.getGuid(), sid, refreshToken);

        return mapper.toResponse(user, accessToken);
    }
}
