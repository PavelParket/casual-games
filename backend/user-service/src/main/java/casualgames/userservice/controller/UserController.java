package casualgames.userservice.controller;

import casualgames.userservice.domain.dto.UpdateUserRequest;
import casualgames.userservice.domain.dto.UserResponse;
import casualgames.userservice.domain.dto.UserSearchFilterRequest;
import casualgames.userservice.service.UserService;
import com.security_starter.config.AuthenticationToken;
import com.security_starter.enums.Role;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping
    public List<UserResponse> findAll(@AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return userService.findAll(authenticationToken);
    }

    @GetMapping("/{guid}")
    public UserResponse findByGuid(@PathVariable UUID guid,
                                   @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return userService.findByGuid(guid, authenticationToken);
    }

    @PostMapping("/search")
    public List<UserResponse> search(@Valid @RequestBody UserSearchFilterRequest request,
                                     @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return userService.search(request, authenticationToken);
    }

    @PutMapping("/{guid}")
    public UserResponse update(@PathVariable UUID guid,
                               @Valid @RequestBody UpdateUserRequest userRequest,
                               @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return userService.update(guid, userRequest, authenticationToken);
    }

    @DeleteMapping("/{guid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteByGuid(@PathVariable UUID guid,
                             @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        userService.deleteByGuid(guid, authenticationToken);
    }

    @PatchMapping("/update-role/{guid}")
    public UserResponse updateRole(@PathVariable UUID guid,
                                   @RequestParam Role role,
                                   @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return userService.updateRole(guid, role, authenticationToken);
    }

    @GetMapping("/balance/{guid}")
    public BigDecimal getBalance(@PathVariable UUID guid,
                                 @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return userService.getBalance(guid, authenticationToken);
    }

    @PostMapping(value = "/attachments/{guid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public UserResponse uploadProfilePicture(@PathVariable UUID guid,
                                             @RequestPart("full") MultipartFile fullFile,
                                             @RequestPart("mini") MultipartFile miniFile,
                                             @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        return userService.uploadImageFile(guid, fullFile, miniFile, authenticationToken);
    }

    @DeleteMapping("/attachments/{guid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProfilePicture(@PathVariable UUID guid,
                                     @AuthenticationPrincipal AuthenticationToken authenticationToken) {
        userService.deleteImageFile(guid, authenticationToken);
    }
}
