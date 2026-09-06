package com.rentflow.integration.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.URI;
import java.net.URL;

/**
 * Validates external outbound URLs to prevent Server-Side Request Forgery (SSRF).
 * Blocks localhost, loopback, link-local, private RFC 1918 IPv4/IPv6 ranges, and non-HTTP protocols.
 */
@Component
public class SsrfProtectionValidator {

    private static final Logger log = LoggerFactory.getLogger(SsrfProtectionValidator.class);

    public static class SsrfSecurityException extends RuntimeException {
        public SsrfSecurityException(String message) {
            super(message);
        }
    }

    /**
     * Validates that a target URL is safe for outbound dispatch.
     * @param urlString target URL string
     * @throws SsrfSecurityException if URL is invalid or targets internal/private infrastructure
     */
    public void validateOutboundUrl(String urlString) {
        if (urlString == null || urlString.trim().isEmpty()) {
            throw new SsrfSecurityException("Outbound URL cannot be null or blank");
        }

        try {
            URI uri = URI.create(urlString.trim());
            String scheme = uri.getScheme();
            if (scheme == null || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                throw new SsrfSecurityException("Protocol '" + scheme + "' is strictly disallowed. Only HTTP and HTTPS are permitted.");
            }

            String host = uri.getHost();
            if (host == null || host.trim().isEmpty()) {
                throw new SsrfSecurityException("Target URL does not specify a valid host.");
            }

            // Reject explicit localhost or link-local hostnames
            if ("localhost".equalsIgnoreCase(host) || host.endsWith(".localhost") || host.endsWith(".local") || host.endsWith(".internal")) {
                throw new SsrfSecurityException("Outbound requests to localhost or internal domain names are forbidden: " + host);
            }

            // Resolve host to IP address and verify it is not in private/reserved ranges
            InetAddress address = InetAddress.getByName(host);

            if (address.isLoopbackAddress()) {
                throw new SsrfSecurityException("Outbound requests to loopback addresses are forbidden: " + address.getHostAddress());
            }

            if (address.isLinkLocalAddress()) {
                throw new SsrfSecurityException("Outbound requests to link-local addresses are forbidden: " + address.getHostAddress());
            }

            if (address.isSiteLocalAddress()) {
                throw new SsrfSecurityException("Outbound requests to private/site-local network ranges are forbidden: " + address.getHostAddress());
            }

            if (address.isAnyLocalAddress() || address.isMulticastAddress()) {
                throw new SsrfSecurityException("Outbound requests to wildcard or multicast addresses are forbidden: " + address.getHostAddress());
            }

            // Additional check for 169.254 (Cloud metadata) and RFC 1918 ranges if not caught by site-local
            byte[] bytes = address.getAddress();
            if (bytes.length == 4) {
                int b0 = bytes[0] & 0xFF;
                int b1 = bytes[1] & 0xFF;

                // 127.0.0.0/8
                if (b0 == 127) {
                    throw new SsrfSecurityException("Forbidden loopback range: " + address.getHostAddress());
                }
                // 10.0.0.0/8
                if (b0 == 10) {
                    throw new SsrfSecurityException("Forbidden private range 10.0.0.0/8: " + address.getHostAddress());
                }
                // 172.16.0.0/12
                if (b0 == 172 && (b1 >= 16 && b1 <= 31)) {
                    throw new SsrfSecurityException("Forbidden private range 172.16.0.0/12: " + address.getHostAddress());
                }
                // 192.168.0.0/16
                if (b0 == 192 && b1 == 168) {
                    throw new SsrfSecurityException("Forbidden private range 192.168.0.0/16: " + address.getHostAddress());
                }
                // 169.254.0.0/16 (AWS/GCP/Azure link-local metadata)
                if (b0 == 169 && b1 == 254) {
                    throw new SsrfSecurityException("Forbidden cloud metadata/link-local address: " + address.getHostAddress());
                }
            }

        } catch (SsrfSecurityException sse) {
            log.warn("SSRF security policy violation: {}", sse.getMessage());
            throw sse;
        } catch (Exception e) {
            log.warn("Failed to validate outbound URL safety: {} - {}", urlString, e.getMessage());
            throw new SsrfSecurityException("Invalid or unresolvable outbound URL: " + e.getMessage());
        }
    }
}
