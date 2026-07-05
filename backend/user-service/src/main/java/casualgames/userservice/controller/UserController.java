package casualgames.userservice.controller;

import casualgames.userservice.domain.dto.UpdateUserRequest;
import casualgames.userservice.domain.dto.UserResponse;
import casualgames.userservice.domain.dto.UserSearchFilterRequest;
import casualgames.userservice.service.UserService;
import com.common_utils.dto.ErrorResponse;
import com.security_starter.enums.Role;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
@ApiResponses(value = {
        @ApiResponse(responseCode = "400",
                description = "Bad Request",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "401",
                description = "Unauthorized",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "403",
                description = "Forbidden",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "404",
                description = "Not Found",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class))),
        @ApiResponse(responseCode = "500",
                description = "Internal Server Error",
                content = @Content(mediaType = "application/json",
                        schema = @Schema(implementation = ErrorResponse.class)))
})
public class UserController {

    private final UserService userService;

    @Deprecated
    @GetMapping
    public List<UserResponse> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{guid}")
    @Operation(summary = "Find user by guid", security = @SecurityRequirement(name = "bearerAuth"))
    public UserResponse findByGuid(@PathVariable UUID guid) {
        return userService.findByGuid(guid);
    }

    @PostMapping("/search")
    @Operation(summary = "Search users by filter", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public List<UserResponse> search(@Valid @RequestBody UserSearchFilterRequest request) {
        return userService.search(request);
    }

    @PutMapping("/{guid}")
    @Operation(summary = "Update user information", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public UserResponse update(@PathVariable UUID guid, @Valid @RequestBody UpdateUserRequest userRequest) {
        return userService.update(guid, userRequest);
    }

    @DeleteMapping("/{guid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user by guid", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204")
    public void deleteByGuid(@PathVariable UUID guid) {
        userService.deleteByGuid(guid);
    }

    @PatchMapping("/update-role/{guid}")
    @Operation(summary = "Update user role", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public UserResponse updateRole(@PathVariable UUID guid, @RequestParam Role role) {
        return userService.updateRole(guid, role);
    }

    @GetMapping("/balance/{guid}")
    @Operation(summary = "Get current user balance", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public BigDecimal getBalance(@PathVariable UUID guid) {
        return userService.getBalance(guid);
    }

    @PostMapping(value = "/attachments/{guid}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload user profile pictures (full and mini versions)", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "200")
    public UserResponse uploadProfilePicture(@PathVariable UUID guid,
                                             @RequestPart("full") MultipartFile fullFile,
                                             @RequestPart("mini") MultipartFile miniFile) {
        return userService.uploadImageFile(guid, fullFile, miniFile);
    }

    @DeleteMapping("/attachments/{guid}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete user profile pictures", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponse(responseCode = "204")
    public void deleteProfilePicture(@PathVariable UUID guid) {
        userService.deleteImageFile(guid);
    }
}
