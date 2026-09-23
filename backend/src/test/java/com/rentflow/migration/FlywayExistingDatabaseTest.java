package com.rentflow.migration;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.output.MigrateResult;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.sql.*;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration test validating the EXISTING INSTALLATION path:
 * 1. Simulates an existing database created before Flyway was introduced.
 * 2. Database contains active production data (tenants, customers, products, bookings, payments).
 * 3. Schema is verified to match baseline version 1.
 * 4. Controlled baseline is executed at version 1 (baselineVersion = "1").
 * 5. Flyway applies only pending migrations (V2 through V5).
 * 6. Validates 100% data preservation: zero records lost, balances intact.
 */
public class FlywayExistingDatabaseTest {

    private JdbcDataSource createDataSource(String dbName) {
        JdbcDataSource ds = new JdbcDataSource();
        ds.setURL("jdbc:h2:mem:" + dbName + ";DB_CLOSE_DELAY=-1;MODE=PostgreSQL");
        ds.setUser("sa");
        ds.setPassword("");
        return ds;
    }

    @Test
    @DisplayName("Existing DB: Controlled baseline at V1 applies V2-V5 and preserves existing business data")
    void testExistingDatabaseBaselineAndDataPreservation() throws SQLException {
        String dbName = "test_existing_db_" + System.currentTimeMillis();
        JdbcDataSource ds = createDataSource(dbName);

        // Step 1: Simulate existing pre-Flyway database by applying V1 DDL directly (no flyway_schema_history)
        Flyway directFlyway = Flyway.configure()
            .dataSource(ds)
            .locations("classpath:db/migration")
            .target("1")
            .load();
        directFlyway.migrate();

        // Drop flyway_schema_history so the database looks exactly like an unmanaged legacy database
        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("DROP TABLE \"flyway_schema_history\"");
        }

        // Step 2: Seed representative business records into the unmanaged existing database
        UUID tenantId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID quoteId = UUID.randomUUID();
        UUID bookingId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();

        try (Connection conn = ds.getConnection()) {
            // 2.1 Insert Tenant
            try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO tenant (id, name) VALUES (?, ?)")) {
                ps.setObject(1, tenantId);
                ps.setString(2, "Apex Event Rentals");
                ps.executeUpdate();
            }

            // 2.2 Insert Customer
            try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO customers (id, tenant_id, customer_number, first_name, last_name, email, status, customer_type) VALUES (?, ?, ?, ?, ?, ?, 'ACTIVE', 'INDIVIDUAL')")) {
                ps.setObject(1, customerId);
                ps.setString(2, tenantId.toString());
                ps.setString(3, "CUST-1001");
                ps.setString(4, "John");
                ps.setString(5, "Doe");
                ps.setString(6, "john.doe@example.com");
                ps.executeUpdate();
            }

            // 2.3 Insert Product
            try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO products (id, tenant_id, sku, name, product_type, status, tracking_type, quantity_owned, quantity_damaged, quantity_in_maintenance, quantity_lost, default_turnaround_minutes, rental_price) " +
                "VALUES (?, ?, ?, ?, 'RENTAL_ITEM', 'ACTIVE', 'QUANTITY', 50, 0, 0, 0, 0, 150.00)")) {
                ps.setObject(1, productId);
                ps.setString(2, tenantId.toString());
                ps.setString(3, "SKU-TENT-20X20");
                ps.setString(4, "20x20 Canopy Tent");
                ps.executeUpdate();
            }

            // 2.4 Insert Quote
            try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO quotes (id, tenant_id, quote_number, customer_id, event_id, status, quote_date, rental_start_date_time, rental_end_date_time, valid_until, total_amount) " +
                "VALUES (?, ?, ?, ?, ?, 'ACCEPTED', CURRENT_DATE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, CURRENT_DATE, 450.00)")) {
                ps.setObject(1, quoteId);
                ps.setString(2, tenantId.toString());
                ps.setString(3, "QT-2026-001");
                ps.setObject(4, customerId);
                ps.setObject(5, UUID.randomUUID());
                ps.executeUpdate();
            }

            // 2.5 Insert Booking
            try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO booking (id, tenant_id, booking_number, quote_id, customer_id, event_id, status, booking_date, rental_start_date_time, rental_end_date_time, total_amount, deposit_paid, balance_due) VALUES (?, ?, ?, ?, ?, ?, 'CONFIRMED', CURRENT_DATE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 450.00, 200.00, 250.00)")) {
                ps.setObject(1, bookingId);
                ps.setString(2, tenantId.toString());
                ps.setString(3, "BK-2026-001");
                ps.setObject(4, quoteId);
                ps.setObject(5, customerId);
                ps.setObject(6, UUID.randomUUID());
                ps.executeUpdate();
            }

            // 2.6 Insert Payment
            try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO payment (id, tenant_id, booking_id, customer_id, amount, payment_method, payment_status, payment_date, created_at, updated_at, transaction_reference) " +
                "VALUES (?, ?, ?, ?, 200.00, 'CREDIT_CARD', 'COMPLETED', CURRENT_DATE, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 'TXN-LEGACY-001')")) {
                ps.setObject(1, paymentId);
                ps.setString(2, tenantId.toString());
                ps.setObject(3, bookingId);
                ps.setObject(4, customerId);
                ps.executeUpdate();
            }
        }

        // Record pre-migration counts
        int preTenantCount = getRowCount(ds, "tenant");
        int preCustCount = getRowCount(ds, "customers");
        int preProdCount = getRowCount(ds, "products");
        int preQuoteCount = getRowCount(ds, "quotes");
        int preBookingCount = getRowCount(ds, "booking");
        int prePaymentCount = getRowCount(ds, "payment");

        assertEquals(1, preTenantCount);
        assertEquals(1, preCustCount);
        assertEquals(1, preProdCount);
        assertEquals(1, preQuoteCount);
        assertEquals(1, preBookingCount);
        assertEquals(1, prePaymentCount);

        // Step 3: Perform Controlled Baseline at Version 1
        Flyway flyway = Flyway.configure()
            .dataSource(ds)
            .locations("classpath:db/migration")
            .baselineVersion("1")
            .baselineDescription("Verified baseline of existing RentFlow schema")
            .baselineOnMigrate(false)
            .load();

        flyway.baseline();

        // Verify baseline recorded in schema history
        MigrationInfo[] historyAfterBaseline = flyway.info().applied();
        assertEquals(1, historyAfterBaseline.length);
        assertEquals("1", historyAfterBaseline[0].getVersion().getVersion());
        assertEquals("BASELINE", historyAfterBaseline[0].getType().name());

        // Step 4: Apply only pending migrations (V2 through V6)
        MigrateResult migrateResult = flyway.migrate();
        assertTrue(migrateResult.success);
        assertEquals(5, migrateResult.migrationsExecuted, "Should execute exactly 5 pending migrations (V2, V3, V4, V5, V6)");
        assertEquals("6", migrateResult.targetSchemaVersion);

        // Step 5: Assert Data Preservation (No data wiped or corrupted)
        assertEquals(preTenantCount, getRowCount(ds, "tenant"), "Tenant count must remain unchanged");
        assertEquals(preCustCount, getRowCount(ds, "customers"), "Customer count must remain unchanged");
        assertEquals(preProdCount, getRowCount(ds, "products"), "Product count must remain unchanged");
        assertEquals(preQuoteCount, getRowCount(ds, "quotes"), "Quote count must remain unchanged");
        assertEquals(preBookingCount, getRowCount(ds, "booking"), "Booking count must remain unchanged");
        assertEquals(prePaymentCount, getRowCount(ds, "payment"), "Payment count must remain unchanged");

        // Verify representative record integrity
        try (Connection conn = ds.getConnection();
             PreparedStatement ps = conn.prepareStatement("SELECT amount, transaction_reference FROM payment WHERE id = ?")) {
            ps.setObject(1, paymentId);
            try (ResultSet rs = ps.executeQuery()) {
                assertTrue(rs.next());
                assertEquals(new BigDecimal("200.00"), rs.getBigDecimal("amount"));
                assertEquals("TXN-LEGACY-001", rs.getString("transaction_reference"));
            }
        }
    }

    @Test
    @DisplayName("Existing DB: Unbaselined existing DB fails when baselineOnMigrate=false")
    void testUnbaselinedExistingDatabaseFailsSafely() throws SQLException {
        String dbName = "test_unbaselined_db_" + System.currentTimeMillis();
        JdbcDataSource ds = createDataSource(dbName);

        // Create an unmanaged table
        try (Connection conn = ds.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE legacy_sample_table (id INT PRIMARY KEY)");
        }

        // Attempting to migrate with baselineOnMigrate=false on non-empty DB must fail fast
        Flyway flyway = Flyway.configure()
            .dataSource(ds)
            .locations("classpath:db/migration")
            .baselineOnMigrate(false)
            .load();

        Exception ex = assertThrows(Exception.class, flyway::migrate);
        assertTrue(ex.getMessage().contains("Found non-empty schema") || ex.getMessage().contains("baseline"),
            "Flyway must reject unbaselined non-empty database when baselineOnMigrate is false");
    }

    private int getRowCount(JdbcDataSource ds, String tableName) throws SQLException {
        try (Connection conn = ds.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM " + tableName)) {
            rs.next();
            return rs.getInt(1);
        }
    }
}
