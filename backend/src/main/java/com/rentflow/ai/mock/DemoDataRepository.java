package com.rentflow.ai.mock;

import org.springframework.stereotype.Repository;

import java.util.*;

@Repository
public class DemoDataRepository {

    public static final String EVERGREEN_TENANT_ID = "99999999-9999-9999-9999-999999999999";

    // Demo Data Containers
    private final List<Map<String, Object>> customers = new ArrayList<>();
    private final List<Map<String, Object>> leads = new ArrayList<>();
    private final List<Map<String, Object>> products = new ArrayList<>();
    private final List<Map<String, Object>> bookings = new ArrayList<>();
    private final List<Map<String, Object>> quotes = new ArrayList<>();
    private final List<Map<String, Object>> deliveries = new ArrayList<>();
    private final List<Map<String, Object>> warehouseTasks = new ArrayList<>();

    public DemoDataRepository() {
        initMockData();
    }

    private Map<String, Object> mapOf(Object... keyValues) {
        Map<String, Object> map = new HashMap<>();
        for (int i = 0; i < keyValues.length; i += 2) {
            map.put((String) keyValues[i], keyValues[i + 1]);
        }
        return map;
    }

    private void initMockData() {
        // Customers
        customers.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "customerId", "customer-001",
            "name", "Emily Brown",
            "city", "Dallas",
            "state", "TX",
            "email", "emily.brown@example.com",
            "phone", "(214) 555-0192",
            "activeBookings", 1
        ));
        customers.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "customerId", "customer-002",
            "name", "TechCorp Annual Gala",
            "city", "Austin",
            "state", "TX",
            "email", "events@techcorp.com",
            "phone", "(512) 555-0144",
            "activeBookings", 1
        ));
        customers.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "customerId", "customer-003",
            "name", "Sarah Jenkins",
            "city", "Fort Worth",
            "state", "TX",
            "email", "sjenkins@example.com",
            "phone", "(817) 555-0833",
            "activeBookings", 1
        ));
        customers.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "customerId", "customer-004",
            "name", "Austin Festival Committee",
            "city", "Austin",
            "state", "TX",
            "email", "info@austinfestival.org",
            "phone", "(512) 555-0991",
            "activeBookings", 1
        ));
        customers.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "customerId", "customer-005",
            "name", "Fairview Event Hall",
            "city", "Arlington",
            "state", "TX",
            "email", "contact@fairviewhall.com",
            "phone", "(817) 555-0422",
            "activeBookings", 1
        ));

        // Leads (8)
        for (int i = 1; i <= 8; i++) {
            leads.add(mapOf(
                "tenantId", EVERGREEN_TENANT_ID,
                "leadId", "lead-00" + i,
                "customerName", i == 1 ? "Emily Brown" : "Lead Customer #" + i,
                "eventType", i % 2 == 0 ? "Corporate Event" : "Wedding",
                "estimatedGuestCount", 150 + (i * 20),
                "status", i <= 3 ? "NEW" : "QUALIFIED"
            ));
        }

        // Products (10)
        products.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "productId", "chair-001",
            "name", "Chiavari Chairs",
            "category", "Seating",
            "totalInventory", 350,
            "availableQuantity", 300,
            "rentalPrice", 8.00
        ));
        products.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "productId", "table-001",
            "name", "60-inch Round Tables",
            "category", "Tables",
            "totalInventory", 50,
            "availableQuantity", 45,
            "rentalPrice", 18.00
        ));
        products.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "productId", "linen-001",
            "name", "White Polyester Linens (120-inch)",
            "category", "Linens",
            "totalInventory", 100,
            "availableQuantity", 85,
            "rentalPrice", 12.00
        ));
        products.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "productId", "tent-001",
            "name", "40x60 High Peak Frame Tent",
            "category", "Tents",
            "totalInventory", 5,
            "availableQuantity", 3,
            "rentalPrice", 1200.00
        ));
        products.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "productId", "audio-001",
            "name", "Pro Event Sound System",
            "category", "Audio/Visual",
            "totalInventory", 8,
            "availableQuantity", 6,
            "rentalPrice", 350.00
        ));
        products.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "productId", "light-001",
            "name", "Warm White Bistro String Lights (100ft)",
            "category", "Lighting",
            "totalInventory", 40,
            "availableQuantity", 30,
            "rentalPrice", 45.00
        ));
        products.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "productId", "stage-001",
            "name", "Modular Stage Platforms (4x8ft)",
            "category", "Staging",
            "totalInventory", 20,
            "availableQuantity", 16,
            "rentalPrice", 65.00
        ));
        products.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "productId", "heater-001",
            "name", "Commercial Propio Patio Heaters",
            "category", "Climate Control",
            "totalInventory", 15,
            "availableQuantity", 10,
            "rentalPrice", 75.00
        ));
        products.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "productId", "cocktail-001",
            "name", "High-Top Cocktail Tables",
            "category", "Tables",
            "totalInventory", 30,
            "availableQuantity", 24,
            "rentalPrice", 15.00
        ));
        products.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "productId", "dance-001",
            "name", "Oak Parquet Dance Floor (16x16ft)",
            "category", "Flooring",
            "totalInventory", 4,
            "availableQuantity", 2,
            "rentalPrice", 450.00
        ));

        // Bookings (5)
        bookings.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "bookingId", "booking-001",
            "customerId", "customer-001",
            "customerName", "Emily Brown",
            "eventType", "Wedding",
            "eventDate", "2026-09-20",
            "venue", "Dallas Pavilion, Dallas TX",
            "status", "QUOTE_SENT_AWAITING_CONFIRMATION",
            "guestCount", 250,
            "rentalTotal", 4850.00,
            "deliverySetup", 1150.00,
            "totalPrice", 6480.00,
            "estimatedCost", 2920.00,
            "estimatedProfit", 3560.00,
            "estimatedMargin", 54.9,
            "items", List.of(
                "250 Chiavari Chairs ($2,000)",
                "25 Round Tables ($450)",
                "25 White Linens ($300)",
                "Warm White Bistro Lights ($600)",
                "Dance Floor ($450)"
            )
        ));

        bookings.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "bookingId", "booking-002",
            "customerId", "customer-002",
            "customerName", "TechCorp Annual Gala",
            "eventType", "Corporate Gala",
            "eventDate", "2026-09-22",
            "venue", "Austin Convention Center",
            "status", "CONTRACT_PENDING",
            "guestCount", 500,
            "totalPrice", 12500.00,
            "estimatedCost", 5200.00,
            "estimatedProfit", 7300.00,
            "estimatedMargin", 58.4
        ));

        bookings.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "bookingId", "booking-003",
            "customerId", "customer-003",
            "customerName", "Sarah Jenkins",
            "eventType", "Reception",
            "eventDate", "2026-09-19",
            "venue", "Fort Worth Botanic Garden",
            "status", "DEPOSIT_AWAITING",
            "guestCount", 150,
            "totalPrice", 3800.00,
            "estimatedCost", 1700.00,
            "estimatedProfit", 2100.00,
            "estimatedMargin", 55.2
        ));

        bookings.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "bookingId", "booking-004",
            "customerId", "customer-004",
            "customerName", "Austin Festival Committee",
            "eventType", "Outdoor Festival",
            "eventDate", "2026-09-25",
            "venue", "Zilker Park, Austin TX",
            "status", "CONFIRMED",
            "guestCount", 1000,
            "totalPrice", 18200.00,
            "estimatedCost", 7900.00,
            "estimatedProfit", 10300.00,
            "estimatedMargin", 56.6
        ));

        bookings.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "bookingId", "booking-005",
            "customerId", "customer-005",
            "customerName", "Fairview Event Hall",
            "eventType", "Anniversary Party",
            "eventDate", "2026-09-21",
            "venue", "Fairview Hall, Arlington TX",
            "status", "PREPARING_WAREHOUSE",
            "guestCount", 120,
            "totalPrice", 2950.00,
            "estimatedCost", 1250.00,
            "estimatedProfit", 1700.00,
            "estimatedMargin", 57.6
        ));

        // Quotes (5)
        quotes.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "quoteId", "Q-8492",
            "customerName", "Emily Brown",
            "guestCount", 250,
            "rentalAmount", 4850.00,
            "deliveryAmount", 750.00,
            "setupAmount", 400.00,
            "totalAmount", 6000.00,
            "status", "SENT_AWAITING_CUSTOMER_CONFIRMATION"
        ));

        // Deliveries (4)
        deliveries.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "deliveryId", "del-001",
            "jobName", "Emily Brown Wedding Delivery",
            "scheduledTime", "10:30 AM",
            "destination", "Evergreen Wedding Venue, Dallas TX",
            "assignedDriver", "David Wilson",
            "vehicle", "Truck #4",
            "status", "SCHEDULED"
        ));
        deliveries.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "deliveryId", "del-002",
            "jobName", "Dallas Arboretum Gala Setup",
            "scheduledTime", "01:15 PM",
            "destination", "Dallas Arboretum Pavilion",
            "assignedDriver", "Mark Reynolds",
            "vehicle", "Truck #2",
            "status", "IN_TRANSIT"
        ));
        deliveries.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "deliveryId", "del-003",
            "jobName", "Fair Park Center Delivery",
            "scheduledTime", "03:00 PM",
            "destination", "Fair Park Center, Dallas",
            "assignedDriver", "David Wilson",
            "vehicle", "Truck #1",
            "status", "SCHEDULED"
        ));
        deliveries.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "deliveryId", "del-004",
            "jobName", "Fort Worth Garden Pickup",
            "scheduledTime", "05:30 PM",
            "destination", "Fort Worth Botanic Garden",
            "assignedDriver", "Sam Carter",
            "vehicle", "Truck #3",
            "status", "PENDING_ASSIGNMENT"
        ));

        // Warehouse Tasks (3)
        warehouseTasks.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "taskId", "wh-task-001",
            "type", "PICK_LIST",
            "title", "Pick 250 Chiavari Chairs & 25 Round Tables",
            "bookingId", "booking-001",
            "eventDate", "2026-09-20",
            "status", "READY_FOR_STAGING"
        ));
        warehouseTasks.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "taskId", "wh-task-002",
            "type", "PACK_LIST",
            "title", "Pack 150 Gold Chiavari Chairs & Linens",
            "bookingId", "booking-003",
            "eventDate", "2026-09-19",
            "status", "IN_PROGRESS"
        ));
        warehouseTasks.add(mapOf(
            "tenantId", EVERGREEN_TENANT_ID,
            "taskId", "wh-task-003",
            "type", "RETURNS_INSPECTION",
            "title", "Inspect 6 Returned Linen Crates in Receiving Bay B-4",
            "bookingId", "booking-005",
            "eventDate", "2026-09-18",
            "status", "PENDING_INSPECTION"
        ));
    }

    public List<Map<String, Object>> getCustomers(String tenantId) {
        return filterByTenant(customers, tenantId);
    }

    public List<Map<String, Object>> getLeads(String tenantId) {
        return filterByTenant(leads, tenantId);
    }

    public List<Map<String, Object>> getProducts(String tenantId) {
        return filterByTenant(products, tenantId);
    }

    public List<Map<String, Object>> getBookings(String tenantId) {
        return filterByTenant(bookings, tenantId);
    }

    public List<Map<String, Object>> getQuotes(String tenantId) {
        return filterByTenant(quotes, tenantId);
    }

    public List<Map<String, Object>> getDeliveries(String tenantId) {
        return filterByTenant(deliveries, tenantId);
    }

    public List<Map<String, Object>> getWarehouseTasks(String tenantId) {
        return filterByTenant(warehouseTasks, tenantId);
    }

    private List<Map<String, Object>> filterByTenant(List<Map<String, Object>> list, String tenantId) {
        if (tenantId == null) return List.of();
        return list.stream()
            .filter(item -> tenantId.equals(item.get("tenantId")))
            .toList();
    }
}
