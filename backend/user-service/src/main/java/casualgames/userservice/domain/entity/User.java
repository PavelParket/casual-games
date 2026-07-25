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

    @Permission(Permissions.GUID)
    @Column(unique = true)
    private UUID guid;

    @Permission(value = Permissions.USERNAME)
    private String username;

    @Permission(value = Permissions.EMAIL)
    @Column(unique = true)
    private String email;

    @Permission(Permissions.BALANCE)
    @Column(precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;

    @Permission(Permissions.ROLE)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Role role = Role.USER;

    @Permission(Permissions.STATUS)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private Status status = Status.DEFAULT;

    private String linkProfilePicture;

    private String linkProfilePictureMini;

    @CreationTimestamp
    @Column(updatable = false)
    private Instant createdAt;
}
