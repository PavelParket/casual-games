package com.security_starter.entity;

import com.security_starter.enums.Operation;
import com.security_starter.enums.Permissions;
import com.security_starter.enums.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Deprecated(forRemoval = true)
//@Entity
//@Table(name = "role_permission")
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RolePermission {

    //@Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    //@Enumerated(EnumType.STRING)
    //@Column(nullable = false)
    private Role role;

    //@Enumerated(EnumType.STRING)
    //@Column(nullable = false)
    private Permissions permission;

    //@Enumerated(EnumType.STRING)
    //@Column(nullable = false)
    private Operation operation;

    //@Column(nullable = false)
    private boolean forMe;

    //@Column(nullable = false)
    private boolean forAll;

    //@CreationTimestamp
    //@Column(nullable = false, updatable = false)
    private Instant createdAt;
}
