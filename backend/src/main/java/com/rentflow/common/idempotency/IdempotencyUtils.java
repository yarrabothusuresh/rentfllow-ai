package com.rentflow.common.idempotency;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public final class IdempotencyUtils {

    private IdempotencyUtils() {}

    /**
     * Computes a canonical SHA-256 hash for a rental request or checkout payload.
     * Canonicalizes customer email, dates, items (sorted by product ID and quantity), and monetary values.
     */
    public static String computeRequestFingerprint(String tenantId,
                                                   String customerEmail,
                                                   String rentalStartDate,
                                                   String rentalEndDate,
                                                   List<String> itemsCanonical,
                                                   String estimatedTotal) {
        StringBuilder canonical = new StringBuilder();
        canonical.append("tenant:").append(tenantId != null ? tenantId.trim().toLowerCase() : "").append("|");
        canonical.append("email:").append(customerEmail != null ? customerEmail.trim().toLowerCase() : "").append("|");
        canonical.append("start:").append(rentalStartDate != null ? rentalStartDate.trim() : "").append("|");
        canonical.append("end:").append(rentalEndDate != null ? rentalEndDate.trim() : "").append("|");

        String itemsJoined = "";
        if (itemsCanonical != null) {
            itemsJoined = itemsCanonical.stream()
                    .filter(Objects::nonNull)
                    .map(String::trim)
                    .sorted()
                    .collect(Collectors.joining(";"));
        }
        canonical.append("items:[").append(itemsJoined).append("]|");
        canonical.append("total:").append(estimatedTotal != null ? estimatedTotal.trim() : "0");

        return sha256(canonical.toString());
    }

    public static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 algorithm unavailable", e);
        }
    }
}
