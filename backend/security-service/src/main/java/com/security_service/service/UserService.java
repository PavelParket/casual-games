package com.security_service.service;

import com.security_service.domain.dto.RegisterRequest;
import com.security_service.domain.dto.UpdateRequest;
import com.security_service.domain.dto.UserResponse;
import com.security_service.domain.entity.CustomUserDetails;
import com.security_service.domain.entity.User;
import com.security_service.domain.enums.Role;
import com.security_service.exception.EmailAlreadyExistsException;
import com.security_service.exception.InvalidRoleException;
import com.security_service.exception.UserNotFoundException;
import com.security_service.factory.UserFactory;
import com.security_service.mapper.UserMapper;
import com.security_service.repository.UserRepository;
import com.security_service.validator.UserValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository repository;

    public final UserMapper mapper;

    private final UserFactory factory;

    private final UserValidator validator;

    private final PasswordService passwordService;
    private UserValidator validator1;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email=" + email));

        return new CustomUserDetails(user);
    }

    @Transactional
    public UserResponse create(RegisterRequest request) {
        validator.validateRegister(request);

        User user = factory.createFromRegisterRequest(request);

        return mapper.toResponse(repository.save(user));
    }

    @Transactional
    public UserResponse update(Long id, UpdateRequest request) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id=" + id));

        if (request.email() != null && !request.email().equals(user.getEmail())
                && repository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("User with email=" + request.email() + " already exists!");
        }

        if (request.role() != null && !request.role().isBlank()) {
            boolean exists = Arrays.stream(Role.values())
                    .anyMatch(role -> role.name().equalsIgnoreCase(request.role()));

            if (!exists) {
                throw new InvalidRoleException("Role " + request.role() + " not found!");
            }
        }

        mapper.updateEntity(user, request);

        if (request.password() != null && !request.password().isBlank()) {
            user.setPassword(passwordService.encode(request.password()));
        }

        return mapper.toResponse(repository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        if (!repository.existsById(id)) {
            throw new UserNotFoundException("User not found!");
        }

        repository.deleteById(id);
    }

    public List<UserResponse> getAll() {
        return mapper.toResponseList(repository.findAll());
    }

    public UserResponse getById(Long id) {
        return mapper.toResponse(repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id=" + id)));
    }

    public UserResponse getByEmail(String email) {
        return mapper.toResponse(repository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email=" + email)));
    }
}
