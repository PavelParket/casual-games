package com.security_service.service;

import com.security_service.client.UserServiceClient;
import com.security_service.domain.dto.RegisterRequest;
import com.security_service.domain.dto.UpdateRequest;
import com.security_service.domain.dto.UserResponse;
import com.security_service.domain.entity.CustomUserDetails;
import com.security_service.domain.entity.User;
import com.security_service.exception.ServiceUnavailableException;
import com.security_service.exception.UserNotFoundException;
import com.security_service.mapper.UserMapper;
import com.security_service.repository.UserRepository;
import com.security_service.validator.UserValidator;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService implements UserDetailsService {

    private final UserRepository repository;

    private final UserMapper mapper;

    private final UserValidator validator;

    private final PasswordService passwordService;

    private final UserServiceClient client;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = repository.findByEmail(email)
                .orElseThrow(() -> new UserNotFoundException("User not found with email=" + email));

        return new CustomUserDetails(user);
    }

    @Transactional
    public UserResponse create(RegisterRequest request) {
        validator.validateRegister(request);

        User user = mapper.toEntity(request, passwordService);

        try {
            client.create(mapper.toCreateUserRequest(user));
        } catch (ServiceUnavailableException e) {
            log.error("UserService is unavailable: {}", e.getMessage(), e);
            throw e;
        }

        return mapper.toResponse(repository.save(user));
    }

    @Transactional
    public UserResponse updateById(Long id, UpdateRequest request) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with id=" + id));

        validator.validateUpdate(request);

        mapper.updateEntity(user, request, passwordService);

        return mapper.toResponse(repository.save(user));
    }

    @Transactional
    public UserResponse updateByEntity(@Valid UpdateRequest request) {
        User user = repository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("User not found with email=" + request.email()));

        validator.validateUpdate(request);

        mapper.updateEntity(user, request, passwordService);

        return mapper.toResponse(repository.save(user));
    }

    @Transactional
    public void delete(Long id) {
        validator.validateIdExists(id);

        repository.deleteById(id);
    }

    @Transactional
    public void deleteByEmail(String email) {
        validator.validateEmailNotExists(email);

/*
        try {
            client.delete(email);
        } catch (ServiceUnavailableException e) {
            log.error("UserService is unavailable during delete: {}", e.getMessage());
            throw e;
        }
*/

        repository.deleteByEmail(email);
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
