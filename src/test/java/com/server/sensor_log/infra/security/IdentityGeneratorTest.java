package com.server.sensor_log.infra.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.regex.Pattern;

import org.junit.jupiter.api.Test;

class IdentityGeneratorTest {
    private final IdentityGenerator generator = new IdentityGenerator();

    @Test
    void serialNumbersAreTwelveDigitsAndDoNotStartWithZero() {
        for (int i = 0; i < 100; i++) {
            String serial = generator.generateSerialNumber();
            assertTrue(Pattern.matches("[1-9][0-9]{11}", serial), serial);
        }
    }

    @Test
    void userIdsAreValidUniqueUuidStrings() {
        String first = generator.generateUserId();
        String second = generator.generateUserId();

        assertDoesNotThrow(() -> java.util.UUID.fromString(first));
        assertDoesNotThrow(() -> java.util.UUID.fromString(second));
        assertNotEquals(first, second);
    }

    @Test
    void tokensHaveExpectedUrlSafeLengthsAndAreDifferent() {
        String bootstrap = generator.generateBootstrapToken();
        String redeem = generator.generateRedeemToken();

        assertAll(
                () -> assertTrue(bootstrap.matches("[A-Za-z0-9_-]{11}")),
                () -> assertTrue(redeem.matches("[A-Za-z0-9_-]{6}")),
                () -> assertNotEquals(bootstrap, generator.generateBootstrapToken()),
                () -> assertNotEquals(redeem, generator.generateRedeemToken()));
    }
}
