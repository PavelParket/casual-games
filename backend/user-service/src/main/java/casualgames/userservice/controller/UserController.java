package casualgames.userservice.controller;

import casualgames.userservice.dto.CreateUserRequest;
import casualgames.userservice.dto.UpdateUserRequest;
import casualgames.userservice.dto.UserResponse;
import casualgames.userservice.dto.UserResponseDto;
import casualgames.userservice.dto.bank_service.TransactionShortInfoInternalRequest;
import casualgames.userservice.service.UserService;
import com.security_starter.enums.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{userId}")
    public UserResponse findById(@PathVariable Long userId) {
        return userService.findById(userId);
    }

    @GetMapping("/username/{username}")
    public List<UserResponse> findByUsername(@PathVariable String username) {
        return userService.findByUsername(username);
    }

    @GetMapping("/email/{email}")
    public UserResponse findByEmail(@PathVariable String email) {
        return userService.findByEmail(email);
    }

    @GetMapping("/guid={guid}")
    public UserResponseDto findByGuid(@PathVariable UUID guid) {
        return userService.findByGuid(guid);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserResponseDto create(@Valid @RequestBody CreateUserRequest userRequest) {
        return userService.create(userRequest);
    }

    @PutMapping("/id={id}")
    public UserResponse update(@PathVariable("id") Long userId,
                               @Valid @RequestBody UpdateUserRequest userRequest) {
        return userService.update(userId, userRequest);
    }

    @PutMapping("/guid={id}")
    public UserResponseDto updateByGuid(@PathVariable("id") UUID guid,
                                        @Valid @RequestBody UpdateUserRequest userRequest) {
        return userService.updateByGuid(guid, userRequest);
    }

    @DeleteMapping("/id={id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Long id) {
        userService.delete(id);
    }

    @DeleteMapping("/guid={id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        userService.deleteByGuid(id);
    }

    @PatchMapping("/guid={guid}")
    public UserResponseDto updateRole(@PathVariable UUID guid, @RequestParam Role role) {
        return userService.updateRole(guid, role);
    }

    @PatchMapping("/update-balance")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public Boolean updateBalances(@RequestBody @Valid List<TransactionShortInfoInternalRequest> transactions) {
        return userService.updateBalances(transactions);
    }

    @GetMapping("/balance/{guid}")
    public BigDecimal getBalance(@PathVariable UUID guid) {
        return userService.getBalance(guid);
    }
}
