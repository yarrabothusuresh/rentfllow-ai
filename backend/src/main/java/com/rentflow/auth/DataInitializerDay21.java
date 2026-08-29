package com.rentflow.auth;

import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.BookingRepository;
import com.rentflow.ai.repository.CustomerRepository;
import com.rentflow.ai.repository.EventRepository;
import com.rentflow.ai.repository.InventoryReservationRepository;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.delivery.model.Delivery;
import com.rentflow.delivery.model.DeliveryPriority;
import com.rentflow.delivery.model.DeliveryStatus;
import com.rentflow.delivery.model.DeliveryType;
import com.rentflow.delivery.model.Driver;
import com.rentflow.delivery.model.DriverStatus;
import com.rentflow.delivery.model.Vehicle;
import com.rentflow.delivery.model.VehicleStatus;
import com.rentflow.delivery.repository.DeliveryRepository;
import com.rentflow.delivery.repository.DriverRepository;
import com.rentflow.delivery.repository.VehicleRepository;
import com.rentflow.warehouse.model.Warehouse;
import com.rentflow.warehouse.repository.WarehouseRepository;
import org.springframework.boot.CommandLineRunner;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Component
@Order(21)
public class DataInitializerDay21 implements CommandLineRunner {

    private final WarehouseRepository warehouseRepository;
    private final ProductRepository productRepository;
    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final CustomerRepository customerRepository;
    private final EventRepository eventRepository;
    private final BookingRepository bookingRepository;
    private final InventoryReservationRepository reservationRepository;
    private final DeliveryRepository deliveryRepository;

    public DataInitializerDay21(WarehouseRepository warehouseRepository,
                               ProductRepository productRepository,
                               DriverRepository driverRepository,
                               VehicleRepository vehicleRepository,
                               CustomerRepository customerRepository,
                               EventRepository eventRepository,
                               BookingRepository bookingRepository,
                               InventoryReservationRepository reservationRepository,
                               DeliveryRepository deliveryRepository) {
        this.warehouseRepository = warehouseRepository;
        this.productRepository = productRepository;
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.customerRepository = customerRepository;
        this.eventRepository = eventRepository;
        this.bookingRepository = bookingRepository;
        this.reservationRepository = reservationRepository;
        this.deliveryRepository = deliveryRepository;
    }

    @Override
    public void run(String... args) throws Exception {
        String tenantId = "tenant-dev";

        // 1. Warehouse
        if (warehouseRepository.findByTenantId(tenantId).isEmpty()) {
            Warehouse wh = new Warehouse(
                    UUID.randomUUID(),
                    tenantId,
                    "WH-MAIN",
                    "Main Warehouse",
                    "100 Logistics Way, Industry City, NY 10001",
                    20, 20, 20, true
            );
            warehouseRepository.save(wh);
        }

        // 2. Inventory Products: 500 Chairs, 200 Tables, 1000 Linens
        Optional<Product> optChair = productRepository.findByTenantId(tenantId).stream()
                .filter(p -> "Chiavary Chair".equalsIgnoreCase(p.getName()) || "CHAIR-001".equalsIgnoreCase(p.getSku()))
                .findFirst();

        Product chair;
        if (optChair.isEmpty()) {
            chair = new Product(
                    UUID.randomUUID(), tenantId, "CHAIR-001", "Chiavary Chair", "Elegant gold Chiavary chair",
                    null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE,
                    new BigDecimal("12.50"), new BigDecimal("85.00"), 500, 0, 0, 0
            );
            chair.setDefaultTurnaroundMinutes(60);
            chair = productRepository.save(chair);
        } else {
            chair = optChair.get();
        }

        Optional<Product> optTable = productRepository.findByTenantId(tenantId).stream()
                .filter(p -> "Banquet Table".equalsIgnoreCase(p.getName()) || "TBL-001".equalsIgnoreCase(p.getSku()))
                .findFirst();

        if (optTable.isEmpty()) {
            Product table = new Product(
                    UUID.randomUUID(), tenantId, "TBL-001", "Banquet Table", "6ft rectangular banquet table",
                    null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE,
                    new BigDecimal("25.00"), new BigDecimal("150.00"), 200, 0, 0, 0
            );
            table.setDefaultTurnaroundMinutes(60);
            productRepository.save(table);
        }

        Optional<Product> optLinen = productRepository.findByTenantId(tenantId).stream()
                .filter(p -> "White Tablecloth".equalsIgnoreCase(p.getName()) || "LINEN-001".equalsIgnoreCase(p.getSku()))
                .findFirst();

        if (optLinen.isEmpty()) {
            Product linen = new Product(
                    UUID.randomUUID(), tenantId, "LINEN-001", "White Tablecloth", "120 inch round white linen",
                    null, ProductType.RENTAL_ITEM, ProductStatus.ACTIVE,
                    new BigDecimal("8.00"), new BigDecimal("45.00"), 1000, 0, 0, 0
            );
            linen.setDefaultTurnaroundMinutes(120);
            productRepository.save(linen);
        }

        // 3. Driver & Vehicle
        Driver driver;
        if (driverRepository.findByTenantId(tenantId).isEmpty()) {
            driver = new Driver();
            driver.setTenantId(tenantId);
            driver.setName("John Smith");
            driver.setPhone("+1-555-0199");
            driver.setLicenseNumber("CDL-12345");
            driver.setStatus(DriverStatus.AVAILABLE);
            driver.setActive(true);
            driver = driverRepository.save(driver);
        } else {
            driver = driverRepository.findByTenantId(tenantId).get(0);
        }

        Vehicle vehicle;
        if (vehicleRepository.findByTenantId(tenantId).isEmpty()) {
            vehicle = new Vehicle();
            vehicle.setTenantId(tenantId);
            vehicle.setVehicleNumber("VAN-01");
            vehicle.setName("Ford Transit 350");
            vehicle.setType("Box Truck");
            vehicle.setCapacity(500);
            vehicle.setStatus(VehicleStatus.AVAILABLE);
            vehicle.setActive(true);
            vehicle = vehicleRepository.save(vehicle);
        } else {
            vehicle = vehicleRepository.findByTenantId(tenantId).get(0);
        }

        // 4. Demo Customer & Event
        Customer customer;
        if (customerRepository.findByTenantId(tenantId).isEmpty()) {
            customer = new Customer();
            customer.setTenantId(tenantId);
            customer.setCustomerNumber("CUST-DAY21");
            customer.setFirstName("Alice");
            customer.setLastName("Johnson");
            customer.setCompanyName("Metropolitan Events Co");
            customer.setEmail("alice@metropolitanevents.com");
            customer.setPhone("+1-555-0200");
            customer.setCustomerType(CustomerType.CORPORATE);
            customer.setStatus(CustomerStatus.ACTIVE);
            customer = customerRepository.save(customer);
        } else {
            customer = customerRepository.findByTenantId(tenantId).get(0);
        }

        Event event;
        if (eventRepository.findByTenantId(tenantId).isEmpty()) {
            event = new Event();
            event.setTenantId(tenantId);
            event.setCustomerId(customer.getId());
            event.setEventName("Grand Autumn Gala");
            event.setEventType(EventType.CORPORATE);
            event.setEventDate(LocalDate.of(2026, 9, 10));
            event.setStartTime("10:00 AM");
            event.setEndTime("05:00 PM");
            event.setVenueName("Grand Ballroom");
            event.setVenueAddress("100 City Center Plaza");
            event.setGuestCount(250);
            event.setStatus(EventStatus.BOOKED);
            event = eventRepository.save(event);
        } else {
            event = eventRepository.findByTenantId(tenantId).get(0);
        }

        // 5. Existing Booking BOOK-000123: Sep 10–12, 400 Chairs
        Optional<Booking> optBooking = bookingRepository.findAll().stream()
                .filter(b -> "BOOK-000123".equalsIgnoreCase(b.getBookingNumber()))
                .findFirst();

        if (optBooking.isEmpty()) {
            Booking booking = new Booking();
            booking.setTenantId(tenantId);
            booking.setBookingNumber("BOOK-000123");
            booking.setQuoteId(UUID.randomUUID());
            booking.setCustomerId(customer.getId());
            booking.setEventId(event.getId());
            booking.setStatus(BookingStatus.CONFIRMED);
            booking.setBookingDate(LocalDate.of(2026, 9, 1));
            booking.setRentalStartDateTime(LocalDateTime.of(2026, 9, 10, 10, 0));
            booking.setRentalEndDateTime(LocalDateTime.of(2026, 9, 12, 17, 0));
            booking.setSubtotal(new BigDecimal("5000.00"));
            booking.setTotalAmount(new BigDecimal("5400.00"));
            booking = bookingRepository.save(booking);

            // Create InventoryReservation for 400 chairs
            InventoryReservation res = new InventoryReservation(
                    UUID.randomUUID(), tenantId, chair.getId(), event.getId(), booking.getId(),
                    400, LocalDateTime.of(2026, 9, 10, 10, 0), LocalDateTime.of(2026, 9, 12, 17, 0),
                    ReservationStatus.CONFIRMED
            );
            reservationRepository.save(res);

            // Delivery assignment: Sep 10 10:00 AM - 12:00 PM for John Smith & VAN-01
            Delivery delivery = new Delivery();
            delivery.setTenantId(tenantId);
            delivery.setDeliveryNumber("DEL-000123");
            delivery.setBookingId(booking.getId());
            delivery.setWarehouseOrderId(UUID.randomUUID());
            delivery.setCustomerId(customer.getId());
            delivery.setEventId(event.getId());
            delivery.setDeliveryType(DeliveryType.DELIVERY);
            delivery.setStatus(DeliveryStatus.SCHEDULED);
            delivery.setPriority(DeliveryPriority.NORMAL);
            delivery.setScheduledDate(LocalDate.of(2026, 9, 10));
            delivery.setScheduledStartTime("10:00 AM");
            delivery.setScheduledEndTime("12:00 PM");
            delivery.setDriverId(driver.getId());
            delivery.setVehicleId(vehicle.getId());
            delivery.setDeliveryAddressSnapshot("100 City Center Plaza");
            deliveryRepository.save(delivery);
        }
    }
}
