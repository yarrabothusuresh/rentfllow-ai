package com.rentflow.permission;

import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "permission")
public class Permission {
    @Id
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(unique = true)
    private PermissionCode code;

    private String description;

    public Permission() {}

    public Permission(UUID id, PermissionCode code, String description) {
        this.id = id;
        this.code = code;
        this.description = description;
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }
    public PermissionCode getCode() { return code; }
    public void setCode(PermissionCode code) { this.code = code; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
