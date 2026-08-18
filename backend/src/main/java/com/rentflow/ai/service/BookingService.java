package com.rentflow.ai.service;

import com.rentflow.ai.dto.*;
import com.rentflow.ai.exception.BookingUnavailableException;
import com.rentflow.ai.model.*;
import com.rentflow.ai.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class BookingService {

    private final BookingRepository bookingRepository;
    private final BookingItemRepository bookingItemRepository;
    private final QuoteRepository quoteRepository;
    private final QuoteItemRepository quoteItemRepository;
    private final InventoryReservationRepository reservationRepository;
    private final InventoryTransactionRepository transactionRepository;
    private final ProductRepository productRepository;
    private final CustomerRepository customerRepository;
    private final EventRepository eventRepository;
    private final AvailabilityService availabilityService;

    public BookingService(BookingRepository bookingRepository,
                          BookingItemRepository bookingItemRepository,
                          QuoteRepository quoteRepository,
                          QuoteItemRepository quoteItemRepository,
                          InventoryReservationRepository reservationRepository,
                          InventoryTransactionRepository transactionRepository,
                          ProductRepository productRepository,
                          CustomerRepository customerRepository,
                          EventRepository eventRepository,
                          AvailabilityService availabilityService) {
        this.bookingRepository = bookingRepository;
        this.bookingItemRepository = bookingItemRepository;
        this.quoteRepository = quoteRepository;
        this.quoteItemRepository = quoteItemRepository;
        this.reservationRepository = reservationRepository;
        this.transactionRepository = transactionRepository;
        this.productRepository = productRepository;
        this.customerRepository = customerRepository;
        this.eventRepository = eventRepository;
        this.availabilityService = availabilityService;
    }

    public synchronized String generateBookingNumber(String tenantId) {
        long count = bookingRepository.countByTenantId(tenantId) + 1;
        return String.format("BKG-%06d", count);
    }

    @Transactional
    public BookingDTO createBookingFromQuote(String tenantId, UUID quoteId, String userRole) {
        // 1. Idempotency Check: if booking already exists for quote, return it
        Optional<Booking> existing = bookingRepository.findByTenantIdAndQuoteId(tenantId, quoteId);
        if (existing.isPresent()) {
            return mapToDTO(existing.get(), userRole);
        }

        // 2. Load Quote
        Quote quote = quoteRepository.findByTenantIdAndId(tenantId, quoteId)
                .orElseThrow(() -> new IllegalArgumentException("Quote not found with ID: " + quoteId));

        // 3. Verify quote status allows booking
        if (quote.getStatus() == QuoteStatus.DRAFT) {
            throw new IllegalStateException("Quote must be sent/accepted before booking.");
        }
        if (quote.getStatus() == QuoteStatus.EXPIRED) {
            throw new IllegalStateException("Quote has expired.");
        }
        if (quote.getStatus() == QuoteStatus.CANCELLED) {
            throw new IllegalStateException("Quote is cancelled.");
        }

        if (quote.getValidUntil() != null && quote.getValidUntil().isBefore(LocalDate.now())) {
            quote.setStatus(QuoteStatus.EXPIRED);
            quoteRepository.save(quote);
            throw new IllegalStateException("Quote has expired.");
        }

        List<QuoteItem> quoteItems = quoteItemRepository.findByQuoteId(quote.getId());
        if (quoteItems.isEmpty()) {
            throw new IllegalStateException("Cannot create booking from a quote with no items.");
        }

        // 4. Recheck availability for EVERY quote item inside transaction
        List<BookingUnavailableDTO.ShortageItemDTO> shortages = new ArrayList<>();
        for (QuoteItem qItem : quoteItems) {
            if (qItem.getProductId() != null) {
                AvailabilityResultDTO avail = availabilityService.checkAvailability(
                        tenantId, qItem.getProductId(), qItem.getQuantity(),
                        quote.getRentalStartDateTime(), quote.getRentalEndDateTime());

                if (!avail.isAvailable()) {
                    shortages.add(new BookingUnavailableDTO.ShortageItemDTO(
                            qItem.getProductId(),
                            avail.getProductName() != null ? avail.getProductName() : qItem.getDescription(),
                            qItem.getQuantity(),
                            avail.getAvailableQuantity(),
                            avail.getShortage(),
                            quote.getRentalStartDateTime(),
                            quote.getRentalEndDateTime()
                    ));
                }
            }
        }

        // 5. If any item is unavailable, STOP and throw structured shortage error
        if (!shortages.isEmpty()) {
            BookingUnavailableDTO errorDTO = new BookingUnavailableDTO();
            errorDTO.setItems(shortages);
            throw new BookingUnavailableException(errorDTO);
        }

        // 6. Create Booking
        Booking booking = new Booking();
        booking.setTenantId(tenantId);
        booking.setBookingNumber(generateBookingNumber(tenantId));
        booking.setQuoteId(quote.getId());
        booking.setCustomerId(quote.getCustomerId());
        booking.setEventId(quote.getEventId());
        booking.setStatus(BookingStatus.CONFIRMED);
        booking.setBookingDate(LocalDate.now());
        booking.setRentalStartDateTime(quote.getRentalStartDateTime());
        booking.setRentalEndDateTime(quote.getRentalEndDateTime());
        booking.setSubtotal(quote.getSubtotal());
        booking.setDiscountAmount(quote.getDiscountAmount());
        booking.setDeliveryFee(quote.getDeliveryFee());
        booking.setPickupFee(quote.getPickupFee());
        booking.setSetupFee(quote.getSetupFee());
        booking.setBreakdownFee(quote.getBreakdownFee());
        booking.setServiceFee(quote.getServiceFee());
        booking.setTaxAmount(quote.getTaxAmount());
        booking.setTotalAmount(quote.getTotalAmount());
        booking.setDepositRequired(quote.getDepositAmount());
        booking.setDepositPaid(BigDecimal.ZERO);
        booking.setBalanceDue(quote.getTotalAmount());
        booking.setNotes(quote.getNotes());
        booking.setInternalNotes(quote.getInternalNotes());
        booking.setCreatedBy(userRole != null ? userRole : "System");

        Booking savedBooking = bookingRepository.save(booking);

        // 7. Copy Quote Items to Booking Items (Snapshot)
        for (QuoteItem qItem : quoteItems) {
            BookingItem bItem = new BookingItem();
            bItem.setBookingId(savedBooking.getId());
            bItem.setProductId(qItem.getProductId());
            bItem.setDescription(qItem.getDescription());
            bItem.setQuantity(qItem.getQuantity());
            bItem.setUnitPrice(qItem.getUnitPrice());
            bItem.setRentalStartDateTime(quote.getRentalStartDateTime());
            bItem.setRentalEndDateTime(quote.getRentalEndDateTime());
            bItem.setLineSubtotal(qItem.getLineSubtotal());
            bookingItemRepository.save(bItem);

            // 8. Create Inventory Reservation
            if (qItem.getProductId() != null) {
                InventoryReservation res = new InventoryReservation();
                res.setTenantId(tenantId);
                res.setProductId(qItem.getProductId());
                res.setEventId(quote.getEventId());
                res.setBookingId(savedBooking.getId());
                res.setQuantity(qItem.getQuantity());
                res.setStartDateTime(quote.getRentalStartDateTime());
                res.setEndDateTime(quote.getRentalEndDateTime());
                res.setStatus(ReservationStatus.RESERVED);
                reservationRepository.save(res);

                // 9. Create Inventory Transaction
                InventoryTransaction tx = new InventoryTransaction(
                        UUID.randomUUID(),
                        tenantId,
                        qItem.getProductId(),
                        TransactionType.RESERVATION,
                        qItem.getQuantity(),
                        "BOOKING",
                        savedBooking.getId(),
                        "Inventory reserved for booking " + savedBooking.getBookingNumber(),
                        userRole
                );
                transactionRepository.save(tx);
            }
        }

        // 10. Update Quote Status to ACCEPTED
        quote.setStatus(QuoteStatus.ACCEPTED);
        quoteRepository.save(quote);

        return mapToDTO(savedBooking, userRole);
    }

    @Transactional
    public BookingDTO confirmBooking(String tenantId, UUID bookingId, String userRole) {
        Booking booking = bookingRepository.findByTenantIdAndId(tenantId, bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        if (booking.getStatus() == BookingStatus.CONFIRMED) {
            return mapToDTO(booking, userRole); // Idempotent
        }

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            throw new IllegalStateException("Cannot confirm a cancelled booking.");
        }

        List<BookingItem> items = bookingItemRepository.findByBookingId(booking.getId());

        // Recheck availability inside transaction
        List<BookingUnavailableDTO.ShortageItemDTO> shortages = new ArrayList<>();
        for (BookingItem item : items) {
            if (item.getProductId() != null) {
                AvailabilityResultDTO avail = availabilityService.checkAvailability(
                        tenantId, item.getProductId(), item.getQuantity(),
                        booking.getRentalStartDateTime(), booking.getRentalEndDateTime());

                if (!avail.isAvailable()) {
                    shortages.add(new BookingUnavailableDTO.ShortageItemDTO(
                            item.getProductId(), item.getDescription(), item.getQuantity(),
                            avail.getAvailableQuantity(), avail.getShortage(),
                            booking.getRentalStartDateTime(), booking.getRentalEndDateTime()
                    ));
                }
            }
        }

        if (!shortages.isEmpty()) {
            BookingUnavailableDTO errorDTO = new BookingUnavailableDTO();
            errorDTO.setItems(shortages);
            throw new BookingUnavailableException(errorDTO);
        }

        // Create reservations if not existing
        List<InventoryReservation> existingRes = reservationRepository.findByTenantId(tenantId).stream()
                .filter(r -> booking.getId().equals(r.getBookingId()))
                .collect(Collectors.toList());

        if (existingRes.isEmpty()) {
            for (BookingItem item : items) {
                if (item.getProductId() != null) {
                    InventoryReservation res = new InventoryReservation();
                    res.setTenantId(tenantId);
                    res.setProductId(item.getProductId());
                    res.setEventId(booking.getEventId());
                    res.setBookingId(booking.getId());
                    res.setQuantity(item.getQuantity());
                    res.setStartDateTime(booking.getRentalStartDateTime());
                    res.setEndDateTime(booking.getRentalEndDateTime());
                    res.setStatus(ReservationStatus.RESERVED);
                    reservationRepository.save(res);

                    InventoryTransaction tx = new InventoryTransaction(
                            UUID.randomUUID(),
                            tenantId,
                            item.getProductId(),
                            TransactionType.RESERVATION,
                            item.getQuantity(),
                            "BOOKING",
                            booking.getId(),
                            "Inventory reserved for booking confirmation " + booking.getBookingNumber(),
                            userRole
                    );
                    transactionRepository.save(tx);
                }
            }
        } else {
            for (InventoryReservation res : existingRes) {
                res.setStatus(ReservationStatus.RESERVED);
                reservationRepository.save(res);
            }
        }

        booking.setStatus(BookingStatus.CONFIRMED);
        Booking updated = bookingRepository.save(booking);
        return mapToDTO(updated, userRole);
    }

    @Transactional
    public BookingDTO cancelBooking(String tenantId, UUID bookingId, String userRole) {
        Booking booking = bookingRepository.findByTenantIdAndId(tenantId, bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found with ID: " + bookingId));

        if (booking.getStatus() == BookingStatus.CANCELLED) {
            return mapToDTO(booking, userRole); // Idempotent
        }

        booking.setStatus(BookingStatus.CANCELLED);
        Booking saved = bookingRepository.save(booking);

        // Find and release inventory reservations
        List<InventoryReservation> reservations = reservationRepository.findByTenantId(tenantId).stream()
                .filter(r -> booking.getId().equals(r.getBookingId()))
                .collect(Collectors.toList());

        for (InventoryReservation res : reservations) {
            if (res.getStatus() != ReservationStatus.RELEASED && res.getStatus() != ReservationStatus.CANCELLED) {
                res.setStatus(ReservationStatus.RELEASED);
                reservationRepository.save(res);

                // Create RELEASE transaction
                InventoryTransaction tx = new InventoryTransaction(
                        UUID.randomUUID(),
                        tenantId,
                        res.getProductId(),
                        TransactionType.RELEASE,
                        res.getQuantity(),
                        "BOOKING",
                        booking.getId(),
                        "Inventory released upon booking cancellation " + booking.getBookingNumber(),
                        userRole
                );
                transactionRepository.save(tx);
            }
        }

        return mapToDTO(saved, userRole);
    }

    public List<BookingDTO> getBookings(String tenantId, String userRole) {
        return bookingRepository.findByTenantId(tenantId).stream()
                .map(b -> mapToDTO(b, userRole))
                .collect(Collectors.toList());
    }

    public Optional<BookingDTO> getBookingById(String tenantId, UUID id, String userRole) {
        return bookingRepository.findByTenantIdAndId(tenantId, id)
                .map(b -> mapToDTO(b, userRole));
    }

    public BookingDTO mapToDTO(Booking b, String userRole) {
        BookingDTO dto = new BookingDTO();
        dto.setId(b.getId());
        dto.setTenantId(b.getTenantId());
        dto.setBookingNumber(b.getBookingNumber());
        dto.setQuoteId(b.getQuoteId());
        dto.setCustomerId(b.getCustomerId());
        dto.setEventId(b.getEventId());
        dto.setStatus(b.getStatus());
        dto.setBookingDate(b.getBookingDate());
        dto.setRentalStartDateTime(b.getRentalStartDateTime());
        dto.setRentalEndDateTime(b.getRentalEndDateTime());
        dto.setSubtotal(b.getSubtotal());
        dto.setDiscountAmount(b.getDiscountAmount());
        dto.setDeliveryFee(b.getDeliveryFee());
        dto.setPickupFee(b.getPickupFee());
        dto.setSetupFee(b.getSetupFee());
        dto.setBreakdownFee(b.getBreakdownFee());
        dto.setServiceFee(b.getServiceFee());
        dto.setTaxAmount(b.getTaxAmount());
        dto.setTotalAmount(b.getTotalAmount());
        dto.setDepositRequired(b.getDepositRequired());
        dto.setDepositPaid(b.getDepositPaid());
        dto.setBalanceDue(b.getBalanceDue());
        dto.setNotes(b.getNotes());
        dto.setCreatedBy(b.getCreatedBy());
        dto.setCreatedAt(b.getCreatedAt());
        dto.setUpdatedAt(b.getUpdatedAt());

        if ("CUSTOMER".equalsIgnoreCase(userRole)) {
            dto.setInternalNotes(null);
        } else {
            dto.setInternalNotes(b.getInternalNotes());
        }

        // Map Quote Number
        if (b.getQuoteId() != null) {
            quoteRepository.findById(b.getQuoteId()).ifPresent(q -> dto.setQuoteNumber(q.getQuoteNumber()));
        }

        // Map Customer Name
        if (b.getCustomerId() != null) {
            customerRepository.findById(b.getCustomerId()).ifPresent(c ->
                dto.setCustomerName((c.getFirstName() + " " + (c.getLastName() != null ? c.getLastName() : "")).trim()));
        }

        // Map Event Name
        if (b.getEventId() != null) {
            eventRepository.findById(b.getEventId()).ifPresent(e -> dto.setEventName(e.getEventName()));
        }

        // Map Booking Items
        List<BookingItem> items = bookingItemRepository.findByBookingId(b.getId());
        List<BookingItemDTO> itemDTOs = new ArrayList<>();
        for (BookingItem item : items) {
            BookingItemDTO idto = new BookingItemDTO();
            idto.setId(item.getId());
            idto.setBookingId(item.getBookingId());
            idto.setProductId(item.getProductId());
            idto.setDescription(item.getDescription());
            idto.setQuantity(item.getQuantity());
            idto.setUnitPrice(item.getUnitPrice());
            idto.setRentalStartDateTime(item.getRentalStartDateTime());
            idto.setRentalEndDateTime(item.getRentalEndDateTime());
            idto.setLineSubtotal(item.getLineSubtotal());
            idto.setReservationStatus(b.getStatus() == BookingStatus.CONFIRMED ? "RESERVED" : b.getStatus().name());
            idto.setCreatedAt(item.getCreatedAt());
            idto.setUpdatedAt(item.getUpdatedAt());

            if (item.getProductId() != null) {
                productRepository.findById(item.getProductId()).ifPresent(p -> idto.setProductName(p.getName()));
            }

            itemDTOs.add(idto);
        }
        dto.setItems(itemDTOs);

        // Map Inventory Reservations
        List<InventoryReservation> resList = reservationRepository.findByTenantId(b.getTenantId()).stream()
                .filter(r -> b.getId().equals(r.getBookingId()))
                .collect(Collectors.toList());

        List<InventoryReservationDTO> resDTOs = new ArrayList<>();
        for (InventoryReservation r : resList) {
            resDTOs.add(availabilityService.mapReservationToDTO(r));
        }
        dto.setReservations(resDTOs);

        return dto;
    }
}
