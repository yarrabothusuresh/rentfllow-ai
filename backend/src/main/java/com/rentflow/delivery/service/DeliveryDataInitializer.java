package com.rentflow.delivery.service;

import com.rentflow.ai.mock.DemoDataRepository;
import com.rentflow.delivery.model.*;
import com.rentflow.delivery.repository.*;
import com.rentflow.warehouse.service.WarehouseDataInitializer;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Component
@Order(6) // Run after warehouse initializers
public class DeliveryDataInitializer implements CommandLineRunner {

    private final DriverRepository driverRepository;
    private final VehicleRepository vehicleRepository;
    private final DeliveryRepository deliveryRepository;

    public static UUID DRIVER_JOHN_ID;
    public static UUID DRIVER_MIKE_ID;
    public static UUID DRIVER_DAVID_ID;

    public static UUID VEHICLE_VAN01_ID;
    public static UUID VEHICLE_VAN02_ID;
    public static UUID VEHICLE_TRUCK01_ID;

    public static UUID DEMO_DELIVERY_1_ID;
    public static UUID DEMO_DELIVERY_2_ID;
    public static UUID DEMO_DELIVERY_3_ID;

    public DeliveryDataInitializer(DriverRepository driverRepository,
                                   VehicleRepository vehicleRepository,
                                   DeliveryRepository deliveryRepository) {
        this.driverRepository = driverRepository;
        this.vehicleRepository = vehicleRepository;
        this.deliveryRepository = deliveryRepository;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        String tenantId = DemoDataRepository.EVERGREEN_TENANT_ID;

        // 1. Drivers
        if (driverRepository.countByTenantId(tenantId) == 0) {
            Driver john = new Driver();
            john.setTenantId(tenantId);
            john.setName("John Smith");
            john.setPhone("+1-555-0191");
            john.setLicenseNumber("DL-98214");
            john.setStatus(DriverStatus.AVAILABLE);
            john = driverRepository.saveAndFlush(john);
            DRIVER_JOHN_ID = john.getId();

            Driver mike = new Driver();
            mike.setTenantId(tenantId);
            mike.setName("Mike Johnson");
            mike.setPhone("+1-555-0192");
            mike.setLicenseNumber("DL-48192");
            mike.setStatus(DriverStatus.AVAILABLE);
            mike = driverRepository.saveAndFlush(mike);
            DRIVER_MIKE_ID = mike.getId();

            Driver david = new Driver();
            david.setTenantId(tenantId);
            david.setName("David Wilson");
            david.setPhone("+1-555-0193");
            david.setLicenseNumber("DL-38291");
            david.setStatus(DriverStatus.AVAILABLE);
            david = driverRepository.saveAndFlush(david);
            DRIVER_DAVID_ID = david.getId();
        } else {
            List<Driver> drivers = driverRepository.findByTenantId(tenantId);
            for (Driver d : drivers) {
                if ("John Smith".equals(d.getName())) DRIVER_JOHN_ID = d.getId();
                else if ("Mike Johnson".equals(d.getName())) DRIVER_MIKE_ID = d.getId();
                else if ("David Wilson".equals(d.getName())) DRIVER_DAVID_ID = d.getId();
            }
        }

        // 2. Vehicles
        if (vehicleRepository.countByTenantId(tenantId) == 0) {
            Vehicle van01 = new Vehicle();
            van01.setTenantId(tenantId);
            van01.setVehicleNumber("VAN-01");
            van01.setName("Ford Transit");
            van01.setType("Cargo Van");
            van01.setCapacity(250);
            van01.setStatus(VehicleStatus.AVAILABLE);
            van01 = vehicleRepository.saveAndFlush(van01);
            VEHICLE_VAN01_ID = van01.getId();

            Vehicle van02 = new Vehicle();
            van02.setTenantId(tenantId);
            van02.setVehicleNumber("VAN-02");
            van02.setName("Mercedes Sprinter");
            van02.setType("Cargo Van");
            van02.setCapacity(400);
            van02.setStatus(VehicleStatus.AVAILABLE);
            van02 = vehicleRepository.saveAndFlush(van02);
            VEHICLE_VAN02_ID = van02.getId();

            Vehicle truck01 = new Vehicle();
            truck01.setTenantId(tenantId);
            truck01.setVehicleNumber("TRK-01");
            truck01.setName("Box Truck 16ft");
            truck01.setType("Box Truck");
            truck01.setCapacity(1000);
            truck01.setStatus(VehicleStatus.AVAILABLE);
            truck01 = vehicleRepository.saveAndFlush(truck01);
            VEHICLE_TRUCK01_ID = truck01.getId();
        } else {
            List<Vehicle> vehicles = vehicleRepository.findByTenantId(tenantId);
            for (Vehicle v : vehicles) {
                if ("VAN-01".equals(v.getVehicleNumber())) VEHICLE_VAN01_ID = v.getId();
                else if ("VAN-02".equals(v.getVehicleNumber())) VEHICLE_VAN02_ID = v.getId();
                else if ("TRK-01".equals(v.getVehicleNumber())) VEHICLE_TRUCK01_ID = v.getId();
            }
        }

        // 3. Demo Deliveries
        if (deliveryRepository.countByTenantId(tenantId) == 0) {
            LocalDate today = LocalDate.of(2026, 8, 30);
            UUID customerId = UUID.fromString("c3333333-3333-3333-3333-333333333333");
            UUID eventId = UUID.fromString("e4444444-4444-4444-4444-444444444444");
            UUID bookingId1 = WarehouseDataInitializer.DEMO_BOOKING_ID;
            UUID woId1 = UUID.fromString("11111111-1111-1111-1111-111111111123");

            Delivery del1 = new Delivery();
            del1.setTenantId(tenantId);
            del1.setDeliveryNumber("DEL-000123");
            del1.setBookingId(bookingId1);
            del1.setWarehouseOrderId(woId1);
            del1.setCustomerId(customerId);
            del1.setEventId(eventId);
            del1.setDeliveryType(DeliveryType.DELIVERY);
            del1.setStatus(DeliveryStatus.SCHEDULED);
            del1.setPriority(DeliveryPriority.HIGH);
            del1.setScheduledDate(today);
            del1.setScheduledStartTime("10:00");
            del1.setScheduledEndTime("12:00");
            del1.setDeliveryAddressSnapshot("Grand Ballroom, 500 Celebration Blvd, Dallas, TX 75202");
            del1.setDriverId(DRIVER_JOHN_ID);
            del1.setVehicleId(VEHICLE_VAN01_ID);
            del1.setSetupRequired(true);
            del1.setSetupDurationMinutes(60);
            del1 = deliveryRepository.saveAndFlush(del1);
            DEMO_DELIVERY_1_ID = del1.getId();

            Delivery del2 = new Delivery();
            del2.setTenantId(tenantId);
            del2.setDeliveryNumber("DEL-000124");
            del2.setBookingId(bookingId1);
            del2.setWarehouseOrderId(woId1);
            del2.setCustomerId(customerId);
            del2.setEventId(eventId);
            del2.setDeliveryType(DeliveryType.DELIVERY);
            del2.setStatus(DeliveryStatus.SCHEDULED);
            del2.setPriority(DeliveryPriority.NORMAL);
            del2.setScheduledDate(today);
            del2.setScheduledStartTime("13:00");
            del2.setScheduledEndTime("15:00");
            del2.setDeliveryAddressSnapshot("Metropolitan Center, 456 Fifth Ave, New York, NY 10018");
            del2.setDriverId(DRIVER_MIKE_ID);
            del2.setVehicleId(VEHICLE_VAN02_ID);
            del2.setSetupRequired(false);
            del2 = deliveryRepository.saveAndFlush(del2);
            DEMO_DELIVERY_2_ID = del2.getId();

            Delivery del3 = new Delivery();
            del3.setTenantId(tenantId);
            del3.setDeliveryNumber("DEL-000125");
            del3.setBookingId(bookingId1);
            del3.setWarehouseOrderId(woId1);
            del3.setCustomerId(customerId);
            del3.setEventId(eventId);
            del3.setDeliveryType(DeliveryType.DELIVERY);
            del3.setStatus(DeliveryStatus.PENDING);
            del3.setPriority(DeliveryPriority.NORMAL);
            del3.setScheduledDate(today);
            del3.setScheduledStartTime("16:00");
            del3.setScheduledEndTime("18:00");
            del3.setDeliveryAddressSnapshot("DEF Hall, 789 Broadway, New York, NY 10003");
            del3 = deliveryRepository.saveAndFlush(del3);
            DEMO_DELIVERY_3_ID = del3.getId();
        } else {
            List<Delivery> deliveries = deliveryRepository.findByTenantId(tenantId);
            for (Delivery d : deliveries) {
                if ("DEL-000123".equals(d.getDeliveryNumber())) DEMO_DELIVERY_1_ID = d.getId();
                else if ("DEL-000124".equals(d.getDeliveryNumber())) DEMO_DELIVERY_2_ID = d.getId();
                else if ("DEL-000125".equals(d.getDeliveryNumber())) DEMO_DELIVERY_3_ID = d.getId();
            }
        }
    }
}
