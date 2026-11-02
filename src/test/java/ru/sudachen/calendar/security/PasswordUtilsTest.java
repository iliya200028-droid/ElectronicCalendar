
package ru.sudachen.calendar.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilsTest {

    @Test
    void passwordHashShouldBeDifferentFromPassword() {
        String password = "MySecurePassword123";

        String hash = PasswordUtils.hashPassword(password);

        assertNotNull(hash);
        assertNotEquals(password, hash);
    }

    @Test
    void correctPasswordShouldBeAccepted() {
        String password = "TestPassword123";

        String hash = PasswordUtils.hashPassword(password);

        assertTrue(
                PasswordUtils.verifyPassword(password, hash)
        );
    }

    @Test
    void incorrectPasswordShouldBeRejected() {
        String password = "CorrectPassword123";

        String hash = PasswordUtils.hashPassword(password);

        assertFalse(
                PasswordUtils.verifyPassword(
                        "WrongPassword123", hash
                )
        );
    }

    @Test
    void samePasswordShouldHaveDifferentHashes() {
        String password = "SamePassword123";

        String firstHash =
                PasswordUtils.hashPassword(password);

        String secondHash =
                PasswordUtils.hashPassword(password);

        assertNotEquals(firstHash, secondHash);

        assertTrue(
                PasswordUtils.verifyPassword(
                        password, firstHash
                )
        );

        assertTrue(
                PasswordUtils.verifyPassword(
                        password, secondHash
                )
        );
    }
}
