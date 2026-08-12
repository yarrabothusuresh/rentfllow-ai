package com.rentflow.auth;

import com.rentflow.role.Role;
import com.rentflow.role.RoleRepository;
import com.rentflow.role.RoleType;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class RoleService {
    private final RoleRepository roleRepository;

    public RoleService(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    public List<Role> findAll() {
        return roleRepository.findAll();
    }

    public Optional<Role> findByRoleType(RoleType roleType) {
        return roleRepository.findByRoleType(roleType);
    }
}
