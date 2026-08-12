package com.rentflow.auth;

import com.rentflow.permission.Permission;
import com.rentflow.role.Role;
import com.rentflow.role.RoleType;
import com.rentflow.user.User;
import com.rentflow.user.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class AuthController {

    private final RoleService roleService;
    private final PermissionService permissionService;
    private final UserRoleService userRoleService;
    private final UserRepository userRepository;

    public AuthController(RoleService roleService, 
                          PermissionService permissionService, 
                          UserRoleService userRoleService,
                          UserRepository userRepository) {
        this.roleService = roleService;
        this.permissionService = permissionService;
        this.userRoleService = userRoleService;
        this.userRepository = userRepository;
    }

    public record RoleDto(UUID id, String roleType, Set<String> permissions) {}
    public record PermissionDto(String code, String description) {}
    public record UserPermissionsDto(UUID userId, String name, String role, Set<String> permissions) {}

    @GetMapping("/roles")
    public List<RoleDto> getRoles() {
        return roleService.findAll().stream()
            .map(r -> new RoleDto(
                r.getId(),
                r.getRoleType().name(),
                r.getPermissions().stream().map(p -> p.getCode().name()).collect(Collectors.toSet())
            ))
            .collect(Collectors.toList());
    }

    @GetMapping("/permissions")
    public List<PermissionDto> getPermissions() {
        return permissionService.findAll().stream()
            .map(p -> new PermissionDto(p.getCode().name(), p.getDescription()))
            .collect(Collectors.toList());
    }

    @GetMapping("/roles/{role}/permissions")
    public ResponseEntity<Set<String>> getRolePermissions(@PathVariable String role) {
        try {
            RoleType type = RoleType.valueOf(role.toUpperCase());
            Optional<Role> roleOpt = roleService.findByRoleType(type);
            if (roleOpt.isPresent()) {
                Set<String> permissions = roleOpt.get().getPermissions().stream()
                    .map(p -> p.getCode().name())
                    .collect(Collectors.toSet());
                return ResponseEntity.ok(permissions);
            }
        } catch (IllegalArgumentException e) {
            // Role not found
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/users/{userId}/permissions")
    public ResponseEntity<UserPermissionsDto> getUserPermissions(@PathVariable UUID userId) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            String roleName = user.getRoles().stream()
                .map(r -> r.getRoleType().name())
                .findFirst()
                .orElse("NONE");
            Set<String> permissions = user.getRoles().stream()
                .flatMap(r -> r.getPermissions().stream())
                .map(p -> p.getCode().name())
                .collect(Collectors.toSet());
            return ResponseEntity.ok(new UserPermissionsDto(user.getId(), user.getName(), roleName, permissions));
        }
        return ResponseEntity.notFound().build();
    }
}
