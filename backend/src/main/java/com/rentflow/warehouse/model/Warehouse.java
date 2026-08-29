package com.rentflow.warehouse.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "warehouses", indexes = {
    @Index(name = "idx_wh_tenant", columnList = "tenantId"),
    @Index(name = "idx_wh_code", columnList = "code")
})
public class Warehouse {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String tenantId;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String name;

    private String address;

    @Column(nullable = false)
    private int dailyPickCapacity = 20;

    @Column(nullable = false)
    private int dailyPackCapacity = 20;

    @Column(nullable = false)
    private int dailyCheckinCapacity = 20;

    @Column(nullable = false)
    private boolean active = true;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Warehouse() {}

    public Warehouse(UUID id, String tenantId, String code, String name, String address,
                     int dailyPickCapacity, int dailyPackCapacity, int dailyCheckinCapacity, boolean active) {
        this.id = id != null ? id : UUID.randomUUID();
        this.tenantId = tenantId;
        this.code = code;
        this.name = name;
        this.address = address;
        this.dailyPickCapacity = dailyPickCapacity > 0 ? dailyPickCapacity : 20;
        this.dailyPackCapacity = dailyPackCapacity > 0 ? dailyPackCapacity : 20;
        this.dailyCheckinCapacity = dailyCheckinCapacity > 0 ? dailyCheckinCapacity : 20;
        this.active = active;
    }

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public UUID getId() { return id; }
    public void setId(UUID id) { this.id = id; }

    public String getTenantId() { return tenantId; }
    public void setTenantId(String tenantId) { this.tenantId = tenantId; }

    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public int getDailyPickCapacity() { return dailyPickCapacity; }
    public void setDailyPickCapacity(int dailyPickCapacity) { this.dailyPickCapacity = dailyPickCapacity; }

    public int getDailyPackCapacity() { return dailyPackCapacity; }
    public void setDailyPackCapacity(int dailyPackCapacity) { this.dailyPackCapacity = dailyPackCapacity; }

    public int getDailyCheckinCapacity() { return dailyCheckinCapacity; }
    public void setDailyCheckinCapacity(int dailyCheckinCapacity) { this.dailyCheckinCapacity = dailyCheckinCapacity; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }

    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
