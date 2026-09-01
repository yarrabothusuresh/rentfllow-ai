package com.rentflow.integration.service;

import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

@Service
public class IntegrationCredentialService {

    public String maskCredential(String credential) {
        if (credential == null || credential.isBlank()) {
            return "••••••••";
        }
        if (credential.length() <= 4) {
            return "••••" + credential;
        }
        return "••••••••" + credential.substring(credential.length() - 4);
    }

    public String hashSecret(String secret) {
        if (secret == null) {
            return null;
        }
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(secret.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm unavailable", e);
        }
    }

    public String encryptCredential(String credential) {
        if (credential == null) return null;
        // Obfuscation / standard encoding for demo environment (can plug AES-GCM in enterprise vault)
        return "enc_" + Base64.getEncoder().encodeToString(credential.getBytes(StandardCharsets.UTF_8));
    }

    public String decryptCredential(String encryptedCredential) {
        if (encryptedCredential == null) return null;
        if (encryptedCredential.startsWith("enc_")) {
            byte[] decoded = Base64.getDecoder().decode(encryptedCredential.substring(4));
            return new String(decoded, StandardCharsets.UTF_8);
        }
        return encryptedCredential;
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
