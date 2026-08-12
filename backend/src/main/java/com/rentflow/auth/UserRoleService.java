package com.rentflow.auth;

import com.rentflow.role.Role;
import com.rentflow.user.User;
import com.rentflow.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.HashSet;
import java.util.Optional;
import java.util.UUID;

@Service
@Transactional
public class UserRoleService {
    private final UserRepository userRepository;

    public UserRoleService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public boolean hasPermission(UUID userId, String permissionCode) {
        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) return false;
        
        User user = userOpt.get();
        return user.getRoles().stream()
            .flatMap(role -> role.getPermissions().stream())
            .anyMatch(p -> p.getCode().name().equalsIgnoreCase(permissionCode));
    }

    public void assignRole(UUID userId, Role role) {
        userRepository.findById(userId).ifPresent(user -> {
            if (user.getRoles() == null) {
                user.setRoles(new HashSet<>());
            }
            user.getRoles().add(role);
            userRepository.save(user);
        });
    }

    public void removeRole(UUID userId, Role role) {
        userRepository.findById(userId).ifPresent(user -> {
            if (user.getRoles() != null) {
                user.getRoles().remove(role);
                userRepository.save(user);
            }
        });
    }
}
