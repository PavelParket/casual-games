package casualgames.userservice.controller;

import casualgames.userservice.dto.UpdateUserRequest;
import casualgames.userservice.dto.UserResponse;
import casualgames.userservice.dto.UserSearchFilterRequest;
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

    @GetMapping("/{guid}")
    public UserResponse findByGuid(@PathVariable UUID guid) {
        return userService.findByGuid(guid);
    }

    @PostMapping("/search")
    public List<UserResponse> search(@Valid @RequestBody UserSearchFilterRequest request) {
        return userService.search(request);
    }

    @PutMapping("/{guid}")
    public UserResponse update(@PathVariable UUID guid, @Valid @RequestBody UpdateUserRequest userRequest) {
        return userService.update(guid, userRequest);
    }

    @DeleteMapping("/{guid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByGuid(@PathVariable UUID guid) {
        userService.deleteByGuid(guid);
    }

    @PatchMapping("/update-role/{guid}")
    public UserResponse updateRole(@PathVariable UUID guid, @RequestParam Role role) {
        return userService.updateRole(guid, role);
    }

    @GetMapping("/balance/{guid}")
    public BigDecimal getBalance(@PathVariable UUID guid) {
        return userService.getBalance(guid);
    }
}
