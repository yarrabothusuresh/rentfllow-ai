package com.rentflow.auth;

import com.rentflow.permission.Permission;
import com.rentflow.permission.PermissionRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PermissionService {
    private final PermissionRepository permissionRepository;

    public PermissionService(PermissionRepository permissionRepository) {
        this.permissionRepository = permissionRepository;
    }

    public List<Permission> findAll() {
        return permissionRepository.findAll();
    }
}
