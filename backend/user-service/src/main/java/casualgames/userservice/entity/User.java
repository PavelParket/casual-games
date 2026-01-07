package casualgames.userservice.entity;

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
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Data
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    @Permission(Permissions.GUID)
    private UUID guid;

    @Column(nullable = false)
    @Permission(Permissions.USERNAME)
    private String username;

    @Column(unique = true, nullable = false)
    @Permission(Permissions.EMAIL)
    private String email;

    @Column(precision = 19, scale = 2)
    @Permission(Permissions.BALANCE)
    private BigDecimal balance = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Permission(Permissions.ROLE)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Permission(Permissions.STATUS)
    private Status status = Status.DEFAULT;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private Instant createdAt;
}
