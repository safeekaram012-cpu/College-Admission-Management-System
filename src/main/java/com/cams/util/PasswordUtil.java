package com.cams.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * PasswordUtil – lightweight SHA-256 hashing for credentials.
 * Not for production use; use BCrypt / Argon2 in real systems.
 */
public class PasswordUtil {

    private PasswordUtil() {}   // utility class – no instantiation

    /**
     * Hashes a plain-text password using SHA-256 and returns a lowercase hex string.
     *
     * @param plainText the raw password
     * @return 64-character hex digest
     */
    public static String hash(String plainText) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] digest    = md.digest(plainText.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            // SHA-256 is always present in the JDK – this should never happen
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Verifies that a plain-text password matches a stored hash.
     *
     * @param plainText the candidate password
     * @param storedHash the hash retrieved from the database
     * @return true if they match
     */
    public static boolean verify(String plainText, String storedHash) {
        return hash(plainText).equalsIgnoreCase(storedHash);
    }
}
