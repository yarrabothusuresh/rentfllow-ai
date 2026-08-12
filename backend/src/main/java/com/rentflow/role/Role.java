package com.rentflow.role;

import com.rentflow.permission.Permission;
import jakarta.persistence.*;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "role")
public class Role {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    private RoleType roleType;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
        name = "role_permission",
        joinColumns = @JoinColumn(name = "role_id"),
        inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<Permission> permissions;

    public Role() {}

    public Role(UUID id, RoleType roleType, Set<Permission> permissions) {
        this.id = id;
        this.roleType = roleType;
        this.permissions = permissions;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public RoleType getRoleType() { return roleType; }
    public void setRoleType(RoleType roleType) { this.roleType = roleType; }
    public Set<Permission> getPermissions() { return permissions; }
    public void setPermissions(Set<Permission> permissions) { this.permissions = permissions; }
}
