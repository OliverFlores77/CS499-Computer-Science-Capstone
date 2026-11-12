package com.example.cs360week3oliverflores;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

/**
 * Turns passwords into salted PBKDF2 hashes, so the database never stores a
 * readable password.
 *
 * How it works:
 *  - A salt is random data created for each account, so two users with the same
 *    password still end up with different hashes, and precomputed tables of
 *    common passwords are useless.
 *  - PBKDF2 mixes the password and salt through HMAC-SHA256 many times
 *    (ITERATIONS), which makes guessing passwords from a stolen database slow.
 *  - Salts and hashes are stored as hexadecimal text.
 *
 * PBKDF2WithHmacSHA256 is available on Android 8.0 (API 26) and newer.
 *
 * This class uses no Android classes, so it can be unit tested on a computer.
 */
public final class PasswordHasher {

    private static final String ALGORITHM = "PBKDF2WithHmacSHA256";
    private static final int KEY_BITS = 256;
    private static final int SALT_BYTES = 16;

    /** How many times the password is mixed. More is slower to attack, and slower to log in. */
    static final int ITERATIONS = 120000;

    private static final SecureRandom RANDOM = new SecureRandom();
    private static final char[] HEX = "0123456789abcdef".toCharArray();

    // Utility class: nothing to instantiate.
    private PasswordHasher() { }

    /** Creates a new random salt (16 random bytes) as 32 hexadecimal characters. */
    public static String generateSalt() {
        byte[] salt = new byte[SALT_BYTES];
        RANDOM.nextBytes(salt);
        return toHex(salt);
    }

    /** Returns the PBKDF2 hash of the password and salt as 64 hexadecimal characters. */
    public static String hash(String password, String saltHex) {
        return hash(password, saltHex, ITERATIONS);
    }

    // Package-private so unit tests can check the algorithm against published
    // test vectors, which use a small iteration count.
    static String hash(String password, String saltHex, int iterations) {
        PBEKeySpec spec = new PBEKeySpec(password.toCharArray(), fromHex(saltHex), iterations, KEY_BITS);
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance(ALGORITHM);
            return toHex(factory.generateSecret(spec).getEncoded());
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            throw new IllegalStateException("Password hashing is not available on this device", e);
        } finally {
            spec.clearPassword();
        }
    }

    /**
     * Checks a password against a stored salt and hash.
     * The comparison takes the same time whether or not the hashes match,
     * so timing cannot be used to guess a hash.
     */
    public static boolean verify(String password, String saltHex, String expectedHashHex) {
        if (password == null || saltHex == null || expectedHashHex == null) {
            return false;
        }
        String actual = hash(password, saltHex);
        return MessageDigest.isEqual(
                actual.getBytes(StandardCharsets.US_ASCII),
                expectedHashHex.getBytes(StandardCharsets.US_ASCII));
    }

    private static String toHex(byte[] bytes) {
        char[] chars = new char[bytes.length * 2];
        for (int i = 0; i < bytes.length; i++) {
            int value = bytes[i] & 0xFF;
            chars[i * 2] = HEX[value >>> 4];
            chars[i * 2 + 1] = HEX[value & 0x0F];
        }
        return new String(chars);
    }

    private static byte[] fromHex(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex text must have an even length");
        }
        byte[] bytes = new byte[hex.length() / 2];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) Integer.parseInt(hex.substring(i * 2, i * 2 + 2), 16);
        }
        return bytes;
    }
}
