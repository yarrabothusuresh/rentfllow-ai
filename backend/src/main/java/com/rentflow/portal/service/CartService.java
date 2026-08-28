package com.rentflow.portal.service;

import com.rentflow.ai.dto.AvailabilityResultDTO;
import com.rentflow.ai.model.Product;
import com.rentflow.ai.repository.ProductRepository;
import com.rentflow.ai.service.AvailabilityService;
import com.rentflow.portal.dto.CartDTO;
import com.rentflow.portal.dto.CartItemDTO;
import com.rentflow.portal.model.RentalCart;
import com.rentflow.portal.model.RentalCartItem;
import com.rentflow.portal.repository.RentalCartRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class CartService {

    private final RentalCartRepository cartRepository;
    private final ProductRepository productRepository;
    private final AvailabilityService availabilityService;

    public CartService(RentalCartRepository cartRepository,
                       ProductRepository productRepository,
                       AvailabilityService availabilityService) {
        this.cartRepository = cartRepository;
        this.productRepository = productRepository;
        this.availabilityService = availabilityService;
    }

    public CartDTO getCart(String tenantId, String cartToken, UUID customerId) {
        RentalCart cart = getOrCreateCart(tenantId, cartToken, customerId);
        return mapToDTO(cart);
    }

    public CartDTO addItemToCart(String tenantId, String cartToken, UUID customerId, UUID productId, int quantity, LocalDateTime start, LocalDateTime end) {
        RentalCart cart = getOrCreateCart(tenantId, cartToken, customerId);

        Product product = productRepository.findByTenantIdAndId(tenantId, productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        LocalDateTime startDateTime = start != null ? start : LocalDateTime.now().plusDays(1).withHour(8);
        LocalDateTime endDateTime = end != null ? end : startDateTime.plusDays(2).withHour(20);

        // Check if item already exists in cart for same dates
        RentalCartItem existingItem = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst()
                .orElse(null);

        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + Math.max(1, quantity));
            existingItem.setStartDateTime(startDateTime);
            existingItem.setEndDateTime(endDateTime);
        } else {
            RentalCartItem newItem = new RentalCartItem();
            newItem.setCart(cart);
            newItem.setProductId(product.getId());
            newItem.setProductName(product.getName());
            newItem.setSku(product.getSku());
            newItem.setQuantity(Math.max(1, quantity));
            newItem.setUnitPrice(product.getRentalPrice() != null ? product.getRentalPrice() : BigDecimal.ZERO);
            newItem.setStartDateTime(startDateTime);
            newItem.setEndDateTime(endDateTime);
            cart.getItems().add(newItem);
        }

        cart.setExpiresAt(LocalDateTime.now().plusMinutes(30)); // Reset 30-min cart expiration
        RentalCart saved = cartRepository.save(cart);
        return mapToDTO(saved);
    }

    public CartDTO updateCartItem(String tenantId, String cartToken, UUID customerId, UUID itemId, int quantity, LocalDateTime start, LocalDateTime end) {
        RentalCart cart = getOrCreateCart(tenantId, cartToken, customerId);

        RentalCartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(itemId))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + itemId));

        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            item.setQuantity(quantity);
            if (start != null) item.setStartDateTime(start);
            if (end != null) item.setEndDateTime(end);
        }

        cart.setExpiresAt(LocalDateTime.now().plusMinutes(30));
        RentalCart saved = cartRepository.save(cart);
        return mapToDTO(saved);
    }

    public CartDTO removeItemFromCart(String tenantId, String cartToken, UUID customerId, UUID itemId) {
        RentalCart cart = getOrCreateCart(tenantId, cartToken, customerId);

        cart.getItems().removeIf(i -> i.getId().equals(itemId));
        RentalCart saved = cartRepository.save(cart);
        return mapToDTO(saved);
    }

    public CartDTO validateCart(String tenantId, String cartToken, UUID customerId) {
        RentalCart cart = getOrCreateCart(tenantId, cartToken, customerId);
        CartDTO dto = mapToDTO(cart);

        List<String> warnings = new ArrayList<>();
        boolean overallValid = true;

        for (CartItemDTO item : dto.getItems()) {
            AvailabilityResultDTO avail = availabilityService.checkAvailability(
                    tenantId, item.getProductId(), item.getQuantity(),
                    item.getStartDateTime(), item.getEndDateTime());

            item.setAvailable(avail.isAvailable());
            item.setAvailableQuantity(avail.getAvailableQuantity());

            if (!avail.isAvailable()) {
                overallValid = false;
                warnings.add("Only " + avail.getAvailableQuantity() + " " + item.getProductName() + " available for selected dates (requested " + item.getQuantity() + ").");
            }
        }

        dto.setValid(overallValid);
        dto.setWarnings(warnings);
        return dto;
    }

    private RentalCart getOrCreateCart(String tenantId, String cartToken, UUID customerId) {
        String token = (cartToken != null && !cartToken.isBlank()) ? cartToken : "cart-" + UUID.randomUUID().toString();

        return cartRepository.findByTenantIdAndCartToken(tenantId, token)
                .orElseGet(() -> {
                    RentalCart cart = new RentalCart();
                    cart.setTenantId(tenantId);
                    cart.setCartToken(token);
                    cart.setCustomerId(customerId);
                    cart.setExpiresAt(LocalDateTime.now().plusMinutes(30));
                    return cartRepository.save(cart);
                });
    }

    private CartDTO mapToDTO(RentalCart cart) {
        CartDTO dto = new CartDTO();
        dto.setCartToken(cart.getCartToken());
        dto.setExpiresAt(cart.getExpiresAt());

        BigDecimal subtotal = BigDecimal.ZERO;
        List<CartItemDTO> itemDTOs = new ArrayList<>();

        for (RentalCartItem item : cart.getItems()) {
            CartItemDTO idto = new CartItemDTO();
            idto.setId(item.getId());
            idto.setProductId(item.getProductId());
            idto.setProductName(item.getProductName());
            idto.setSku(item.getSku());
            idto.setQuantity(item.getQuantity());
            idto.setUnitPrice(item.getUnitPrice());

            BigDecimal lineSub = item.getUnitPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            idto.setLineSubtotal(lineSub);
            subtotal = subtotal.add(lineSub);

            idto.setStartDateTime(item.getStartDateTime());
            idto.setEndDateTime(item.getEndDateTime());

            // Default availability check snapshot
            if (item.getProductId() != null && item.getStartDateTime() != null && item.getEndDateTime() != null) {
                AvailabilityResultDTO avail = availabilityService.checkAvailability(
                        cart.getTenantId(), item.getProductId(), item.getQuantity(),
                        item.getStartDateTime(), item.getEndDateTime());
                idto.setAvailable(avail.isAvailable());
                idto.setAvailableQuantity(avail.getAvailableQuantity());
            }

            itemDTOs.add(idto);
        }

        dto.setItems(itemDTOs);
        dto.setSubtotal(subtotal);

        BigDecimal tax = subtotal.multiply(new BigDecimal("0.0825")).setScale(2, java.math.RoundingMode.HALF_UP);
        dto.setEstimatedTax(tax);
        dto.setEstimatedTotal(subtotal.add(tax));

        return dto;
    }
}
