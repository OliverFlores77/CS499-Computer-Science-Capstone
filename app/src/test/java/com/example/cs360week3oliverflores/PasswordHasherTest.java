package com.example.cs360week3oliverflores;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Local unit tests for PasswordHasher. They run on the development computer;
 * no emulator is needed.
 */
public class PasswordHasherTest {

    @Test
    public void hash_matchesPublishedPbkdf2Sha256TestVectors() {
        // Known answers for PBKDF2-HMAC-SHA256, password "password", salt "salt"
        // (hex 73616c74), 32-byte output. They prove the algorithm is implemented correctly.
        assertEquals("120fb6cffcf8b32c43e7225256c4f837a86548c92ccc35480805987cb70be17b",
                PasswordHasher.hash("password", "73616c74", 1));
        assertEquals("ae4d0c95af6b46d32d0adff928f06dd02a303f8ef3c251dfd6e2d85a95474c43",
                PasswordHasher.hash("password", "73616c74", 2));
    }

    @Test
    public void hash_isTheSameForTheSamePasswordAndSalt() {
        String salt = PasswordHasher.generateSalt();
        assertEquals(PasswordHasher.hash("secret1", salt), PasswordHasher.hash("secret1", salt));
    }

    @Test
    public void hash_differsWhenTheSaltDiffers() {
        // Two users with the same password must not end up with the same stored hash.
        String saltA = PasswordHasher.generateSalt();
        String saltB = PasswordHasher.generateSalt();
        assertFalse(PasswordHasher.hash("secret1", saltA).equals(PasswordHasher.hash("secret1", saltB)));
    }

    @Test
    public void hash_hasExpectedLengthAndIsNotThePassword() {
        String hash = PasswordHasher.hash("secret1", PasswordHasher.generateSalt());
        assertEquals(64, hash.length());
        assertTrue(hash.matches("[0-9a-f]{64}"));
        assertFalse(hash.contains("secret1"));
    }

    @Test
    public void generateSalt_isRandomHexOfExpectedLength() {
        String first = PasswordHasher.generateSalt();
        String second = PasswordHasher.generateSalt();
        assertEquals(32, first.length());
        assertTrue(first.matches("[0-9a-f]{32}"));
        assertFalse(first.equals(second));
    }

    @Test
    public void verify_acceptsTheRightPasswordAndRejectsWrongOnes() {
        String salt = PasswordHasher.generateSalt();
        String stored = PasswordHasher.hash("secret1", salt);
        assertTrue(PasswordHasher.verify("secret1", salt, stored));
        assertFalse(PasswordHasher.verify("secret2", salt, stored));
        assertFalse(PasswordHasher.verify("", salt, stored));
        assertFalse(PasswordHasher.verify("Secret1", salt, stored));
    }

    @Test
    public void verify_rejectsMissingValues() {
        assertFalse(PasswordHasher.verify(null, "00", "00"));
        assertFalse(PasswordHasher.verify("secret1", null, "00"));
        assertFalse(PasswordHasher.verify("secret1", "00", null));
    }
}
