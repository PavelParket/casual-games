package casualgames.userservice.domain.entity;

import com.security_starter.annotation.Permission;
import com.security_starter.enums.Permissions;
import com.security_starter.enums.Role;
import com.security_starter.enums.Status;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "users")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @Permission(Permissions.GUID)
    private UUID guid;

    @Column(nullable = false)
    @Permission(value = Permissions.USERNAME, deleteAllowed = false)
    private String username;

    @Column(unique = true, nullable = false)
    @Permission(value = Permissions.EMAIL, deleteAllowed = false)
    private String email;

    @Builder.Default
    @Column(precision = 19, scale = 2)
    @Permission(Permissions.BALANCE)
    private BigDecimal balance = BigDecimal.ZERO;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Permission(Permissions.ROLE)
    private Role role = Role.USER;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Permission(Permissions.STATUS)
    private Status status = Status.DEFAULT;

    private String linkProfilePicture;

    private String linkProfilePictureMini;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
