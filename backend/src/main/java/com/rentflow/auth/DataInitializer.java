package com.rentflow.auth;

import com.rentflow.permission.Permission;
import com.rentflow.permission.PermissionCode;
import com.rentflow.permission.PermissionRepository;
import com.rentflow.role.Role;
import com.rentflow.role.RoleRepository;
import com.rentflow.role.RoleType;
import com.rentflow.tenant.Tenant;
import com.rentflow.tenant.TenantRepository;
import com.rentflow.user.User;
import com.rentflow.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.*;

@Component
public class DataInitializer implements CommandLineRunner {

    private final TenantRepository tenantRepository;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PermissionRepository permissionRepository;

    public DataInitializer(TenantRepository tenantRepository,
                           UserRepository userRepository,
                           RoleRepository roleRepository,
                           PermissionRepository permissionRepository) {
        this.tenantRepository = tenantRepository;
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.permissionRepository = permissionRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        // 1. Initialize Tenant
        UUID tenantId = UUID.fromString("99999999-9999-9999-9999-999999999999");
        Tenant tenant = new Tenant(tenantId, "Evergreen Event Rentals");
        tenantRepository.save(tenant);

        // 2. Initialize Permissions
        Map<PermissionCode, Permission> permMap = new HashMap<>();
        for (PermissionCode code : PermissionCode.values()) {
            Permission p = new Permission(
                UUID.randomUUID(),
                code,
                "Permission for " + code.name().replace("_", " ").toLowerCase()
            );
            permissionRepository.save(p);
            permMap.put(code, p);
        }

        // 3. Initialize Roles with Permissions
        // OWNER permissions: All
        Set<Permission> ownerPerms = new HashSet<>(permMap.values());
        Role ownerRole = new Role(UUID.fromString("10000000-0000-0000-0000-000000000000"), RoleType.OWNER, ownerPerms);
        roleRepository.save(ownerRole);

        // ADMIN permissions: All except AI_COPILOT_USE, and COMPANY_SETTINGS_UPDATE/USER_UPDATE for Owner
        Set<Permission> adminPerms = new HashSet<>(permMap.values());
        adminPerms.remove(permMap.get(PermissionCode.AI_COPILOT_USE));
        adminPerms.remove(permMap.get(PermissionCode.COMPANY_SETTINGS_VIEW));
        adminPerms.remove(permMap.get(PermissionCode.COMPANY_SETTINGS_UPDATE));
        Role adminRole = new Role(UUID.fromString("20000000-0000-0000-0000-000000000000"), RoleType.ADMIN, adminPerms);
        roleRepository.save(adminRole);

        // SALES permissions
        Set<Permission> salesPerms = new HashSet<>();
        salesPerms.add(permMap.get(PermissionCode.DASHBOARD_VIEW));
        salesPerms.add(permMap.get(PermissionCode.AI_COPILOT_USE));
        salesPerms.add(permMap.get(PermissionCode.CUSTOMER_VIEW));
        salesPerms.add(permMap.get(PermissionCode.CUSTOMER_CREATE));
        salesPerms.add(permMap.get(PermissionCode.CUSTOMER_UPDATE));
        salesPerms.add(permMap.get(PermissionCode.LEAD_VIEW));
        salesPerms.add(permMap.get(PermissionCode.LEAD_CREATE));
        salesPerms.add(permMap.get(PermissionCode.LEAD_UPDATE));
        salesPerms.add(permMap.get(PermissionCode.PRODUCT_VIEW));
        salesPerms.add(permMap.get(PermissionCode.INVENTORY_VIEW));
        salesPerms.add(permMap.get(PermissionCode.QUOTE_VIEW));
        salesPerms.add(permMap.get(PermissionCode.QUOTE_CREATE));
        salesPerms.add(permMap.get(PermissionCode.QUOTE_UPDATE));
        salesPerms.add(permMap.get(PermissionCode.QUOTE_SEND));
        salesPerms.add(permMap.get(PermissionCode.BOOKING_VIEW));
        salesPerms.add(permMap.get(PermissionCode.BOOKING_CREATE));
        salesPerms.add(permMap.get(PermissionCode.BOOKING_UPDATE));
        salesPerms.add(permMap.get(PermissionCode.DELIVERY_VIEW));
        Role salesRole = new Role(UUID.fromString("30000000-0000-0000-0000-000000000000"), RoleType.SALES, salesPerms);
        roleRepository.save(salesRole);

        // WAREHOUSE permissions
        Set<Permission> whPerms = new HashSet<>();
        whPerms.add(permMap.get(PermissionCode.DASHBOARD_VIEW));
        whPerms.add(permMap.get(PermissionCode.PRODUCT_VIEW));
        whPerms.add(permMap.get(PermissionCode.INVENTORY_VIEW));
        whPerms.add(permMap.get(PermissionCode.INVENTORY_UPDATE));
        whPerms.add(permMap.get(PermissionCode.BOOKING_VIEW));
        whPerms.add(permMap.get(PermissionCode.WAREHOUSE_VIEW));
        whPerms.add(permMap.get(PermissionCode.WAREHOUSE_UPDATE));
        whPerms.add(permMap.get(PermissionCode.DELIVERY_VIEW));
        Role warehouseRole = new Role(UUID.fromString("40000000-0000-0000-0000-000000000000"), RoleType.WAREHOUSE, whPerms);
        roleRepository.save(warehouseRole);

        // DRIVER permissions
        Set<Permission> driverPerms = new HashSet<>();
        driverPerms.add(permMap.get(PermissionCode.DASHBOARD_VIEW));
        driverPerms.add(permMap.get(PermissionCode.BOOKING_VIEW));
        driverPerms.add(permMap.get(PermissionCode.DELIVERY_VIEW));
        driverPerms.add(permMap.get(PermissionCode.DELIVERY_UPDATE));
        Role driverRole = new Role(UUID.fromString("50000000-0000-0000-0000-000000000000"), RoleType.DRIVER, driverPerms);
        roleRepository.save(driverRole);

        // CUSTOMER permissions
        Set<Permission> customerPerms = new HashSet<>();
        customerPerms.add(permMap.get(PermissionCode.STOREFRONT_VIEW));
        customerPerms.add(permMap.get(PermissionCode.DASHBOARD_VIEW));
        customerPerms.add(permMap.get(PermissionCode.CUSTOMER_VIEW));
        customerPerms.add(permMap.get(PermissionCode.QUOTE_VIEW));
        customerPerms.add(permMap.get(PermissionCode.BOOKING_VIEW));
        customerPerms.add(permMap.get(PermissionCode.PAYMENT_VIEW));
        customerPerms.add(permMap.get(PermissionCode.PAYMENT_CREATE));
        Role customerRole = new Role(UUID.fromString("60000000-0000-0000-0000-000000000000"), RoleType.CUSTOMER, customerPerms);
        roleRepository.save(customerRole);

        // 4. Initialize Demo Users
        // Owner - John Anderson
        User owner = new User(
            UUID.fromString("11111111-1111-1111-1111-111111111111"),
            "John Anderson",
            tenant,
            Set.of(ownerRole)
        );
        userRepository.save(owner);

        // Admin - Sarah Miller
        User admin = new User(
            UUID.fromString("22222222-2222-2222-2222-222222222222"),
            "Sarah Miller",
            tenant,
            Set.of(adminRole)
        );
        userRepository.save(admin);

        // Sales - Mike Johnson
        User sales = new User(
            UUID.fromString("33333333-3333-3333-3333-333333333333"),
            "Mike Johnson",
            tenant,
            Set.of(salesRole)
        );
        userRepository.save(sales);

        // Warehouse - Robert Smith
        User warehouse = new User(
            UUID.fromString("44444444-4444-4444-4444-444444444444"),
            "Robert Smith",
            tenant,
            Set.of(warehouseRole)
        );
        userRepository.save(warehouse);

        // Driver - David Wilson
        User driver = new User(
            UUID.fromString("55555555-5555-5555-5555-555555555555"),
            "David Wilson",
            tenant,
            Set.of(driverRole)
        );
        userRepository.save(driver);

        // Customer - Emily Brown (Treat as external customer, null tenant or special customer tenant)
        User customer = new User(
            UUID.fromString("66666666-6666-6666-6666-666666666666"),
            "Emily Brown",
            null, // External Customer
            Set.of(customerRole)
        );
        userRepository.save(customer);
    }
}
