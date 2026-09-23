package com.rentflow.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.output.MigrateResult;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test validating the NEW INSTALLATION path:
 * 1. Empty database is created.
 * 2. Flyway executes versioned migrations V1 through V5.
 * 3. All core tables, constraints, and indexes are created.
 * 4. Schema history is properly recorded.
 * 5. Constraints are behaviorally validated to enforce data integrity.
 */
public class FlywayFreshDatabaseTest {

    private JdbcDataSource createFreshDataSource(String dbName) {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }

    @Test
    @DisplayName("Fresh DB: Flyway applies all V1-V5 migrations in sequence")
    void testFreshDatabaseMigrationExecution() {
        JdbcDataSource ds = createFreshDataSource("test_fresh_db_" + System.currentTimeMillis());

        Flyway flyway = Flyway.configure()
            .dataSource(ds)
            .locations("classpath:db/migration")
            .baselineOnMigrate(false)
            .validateOnMigrate(true)
            .table("flyway_schema_history")
            .load();

        MigrateResult result = flyway.migrate();
        assertTrue(result.success, "Flyway migration should succeed");
        assertEquals(6, result.migrationsExecuted, "Should execute exactly 6 migrations (V1..V6)");
        assertEquals("6", result.targetSchemaVersion, "Target schema version should be 6");

        // Verify Schema History Table Entries
        MigrationInfo[] applied = flyway.info().applied();
        assertEquals(6, applied.length);

        assertEquals("1", applied[0].getVersion().getVersion());
        assertEquals("initial schema", applied[0].getDescription());
        assertEquals("SUCCESS", applied[0].getState().name());

        assertEquals("2", applied[1].getVersion().getVersion());
        assertEquals("security and auth hardening", applied[1].getDescription());
        assertEquals("SUCCESS", applied[1].getState().name());

        assertEquals("3", applied[2].getVersion().getVersion());
        assertEquals("payment idempotency constraints", applied[2].getDescription());
        assertEquals("SUCCESS", applied[2].getState().name());

        assertEquals("4", applied[3].getVersion().getVersion());
        assertEquals("booking checkout idempotency", applied[3].getDescription());
        assertEquals("SUCCESS", applied[3].getState().name());

        assertEquals("5", applied[4].getVersion().getVersion());
        assertEquals("financial precision and inventory constraints", applied[4].getDescription());
        assertEquals("SUCCESS", applied[4].getState().name());

        assertEquals("6", applied[5].getVersion().getVersion());
        assertEquals("database constraints and performance indexes", applied[5].getDescription());
        assertEquals("SUCCESS", applied[5].getState().name());
    }

    @Test
    @DisplayName("Fresh DB: Core business tables exist after migration")
    void testCoreTablesExistAfterMigration() throws SQLException {
        JdbcDataSource ds = createFreshDataSource("test_tables_db_" + System.currentTimeMillis());

        Flyway flyway = Flyway.configure()
            .dataSource(ds)
            .locations("classpath:db/migration")
            .load();
        flyway.migrate();

        List<String> expectedTables = List.of(
            "tenant",
            "app_user",
            "role",
            "permission",
            "user_role",
            "role_permission",
            "customers",
            "customer_addresses",
            "events",
            "products",
            "product_categories",
            "inventory_reservations",
            "quotes",
            "quote_items",
            "booking",
            "booking_item",
            "invoices",
            "invoice_items",
            "payment",
            "payment_audit",
            "rental_requests",
            "crm_leads",
            "warehouses",
            "warehouse_stock",
            "warehouse_orders",
            "deliveries",
            "damage_claims",
            "phone_call_sessions",
            "ai_recommendations",
            "integration_outbox"
        );

        Set<String> actualTables = new HashSet<>();
        try (Connection conn = ds.getConnection()) {
            DatabaseMetaData metaData = conn.getMetaData();
            try (ResultSet rs = metaData.getTables(null, null, "%", new String[]{"TABLE"})) {
                while (rs.next()) {
                    actualTables.add(rs.getString("TABLE_NAME").toLowerCase());
                }
            }
        }

        for (String expected : expectedTables) {
            assertTrue(actualTables.contains(expected.toLowerCase()), 
                "Expected table '" + expected + "' must exist after migration");
        }
    }

    @Test
    @DisplayName("Fresh DB: P0 unique & check constraints are enforced by database engine")
    void testIdempotencyAndSecurityConstraintsEnforced() throws SQLException {
        JdbcDataSource ds = createFreshDataSource("test_constraints_db_" + System.currentTimeMillis());

        Flyway flyway = Flyway.configure()
            .dataSource(ds)
            .locations("classpath:db/migration")
            .load();
        flyway.migrate();

        String tenantId = UUID.randomUUID().toString();

        try (Connection conn = ds.getConnection()) {
            // 1. Validate Payment Idempotency Constraint (uq_payment_tenant_transaction_reference)
            UUID p1 = UUID.randomUUID();
            UUID p2 = UUID.randomUUID();
            String txnRef = "TXN-FRESH-001";
            try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO payment (id, tenant_id, booking_id, customer_id, amount, payment_method, payment_status, payment_date, created_at, updated_at, transaction_reference) " +
                "VALUES (?, ?, ?, ?, 100.00, 'CREDIT_CARD', 'COMPLETED', CURRENT_DATE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, ?)")) {
                ps.setObject(1, p1);
                ps.setString(2, tenantId);
                ps.setObject(3, UUID.randomUUID());
                ps.setObject(4, UUID.randomUUID());
                ps.setString(5, txnRef);
                ps.executeUpdate();

                // Duplicate insert should fail
                ps.setObject(1, p2);
                assertThrows(SQLException.class, ps::executeUpdate, 
                    "Duplicate payment transaction reference for same tenant must be rejected");
            }

            // 2. Validate Booking Quote Deduplication Constraint (uq_booking_tenant_quote)
            UUID b1 = UUID.randomUUID();
            UUID b2 = UUID.randomUUID();
            UUID quoteId = UUID.randomUUID();
            try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO booking (id, tenant_id, booking_number, quote_id, customer_id, event_id, status, booking_date, rental_start_date_time, rental_end_date_time, total_amount, deposit_paid, balance_due) " +
                "VALUES (?, ?, ?, ?, ?, ?, 'CONFIRMED', CURRENT_DATE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 500.00, 100.00, 400.00)")) {
                ps.setObject(1, b1);
                ps.setString(2, tenantId);
                ps.setString(3, "BK-TEST-001");
                ps.setObject(4, quoteId);
                ps.setObject(5, UUID.randomUUID());
                ps.setObject(6, UUID.randomUUID());
                ps.executeUpdate();

                // Duplicate quote conversion must fail
                ps.setObject(1, b2);
                ps.setString(3, "BK-TEST-002");
                assertThrows(SQLException.class, ps::executeUpdate,
                    "Duplicate booking quote conversion for same tenant must be rejected");
            }

            // 3. Validate Physical Inventory Non-Negative Check (chk_products_quantity_owned_nonneg)
            try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO products (id, tenant_id, sku, name, product_type, status, tracking_type, quantity_owned, rental_price) " +
                "VALUES (?, ?, 'SKU-NEG', 'Negative Stock Product', 'RENTAL_ITEM', 'ACTIVE', 'QUANTITY', -5, 10.00)")) {
                ps.setObject(1, UUID.randomUUID());
                ps.setString(2, tenantId);
                assertThrows(SQLException.class, ps::executeUpdate,
                    "Negative quantity_owned must violate chk_products_quantity_owned_nonneg");
            }
        }
    }
}
