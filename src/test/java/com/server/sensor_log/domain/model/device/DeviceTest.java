package com.server.sensor_log.domain.model.device;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.server.sensor_log.application.exceptions.InvalidCodeException;

class DeviceTest {
    private static final String SERIAL = "123456789012";
    private static final String BOOTSTRAP = "bootstrap";
    private static final MetricsType METRIC = MetricsType.TEMPERATURE;

    @Test
    void createInitializesAnUnclaimedOfflineDevice() {
        Device device = Device.create(SERIAL, BOOTSTRAP, "test device", new Location(1.2, 3.4),
                List.of(METRIC, MetricsType.HUMIDITY));

        assertAll(
                () -> assertEquals(SERIAL, device.getSerialNumber()),
                () -> assertEquals(Role.DEVICE, device.getRole()),
                () -> assertEquals("1.0.0", device.getFirmwareVersion()),
                () -> assertEquals(BOOTSTRAP, device.getBootstrapToken()),
                () -> assertNull(device.getRedeemCode()),
                () -> assertEquals(ClaimStatus.UNCLAIMED, device.getClaimStatus()),
                () -> assertEquals(ConnectionStatus.OFFLINE, device.getConnectionStatus()),
                () -> assertEquals(Map.of(METRIC, 0.0, MetricsType.HUMIDITY, 0.0), device.getMetrics()),
                () -> assertNotNull(device.getCreatedAt()));
    }

    @Test
    void restorePreservesAllPersistedStateAndCopiesMetrics() {
        Instant createdAt = Instant.parse("2025-01-01T00:00:00Z");
        Map<MetricsType, Double> metrics = new EnumMap<>(Map.of(METRIC, 22.5));
        Certificate certificate = new Certificate("pem", "serial", createdAt.plusSeconds(10), createdAt);
        RedeemCode redeemCode = RedeemCode.restore("redeem", createdAt.plusSeconds(60), 0,
                Instant.EPOCH, mock(RandomGenerator.class));

        Device device = Device.restore(SERIAL, "owner", certificate, createdAt.plusSeconds(5),
                ClaimStatus.CLAIMED, ConnectionStatus.ONLINE, new Location(1, 2), "description",
                metrics, "bootstrap-hash", redeemCode, createdAt);
        metrics.put(METRIC, 99.0);

        assertAll(
                () -> assertEquals("owner", device.getOwnerId()),
                () -> assertEquals(certificate, device.getCertificate()),
                () -> assertEquals(createdAt.plusSeconds(5), device.getClaimedAt()),
                () -> assertEquals(ClaimStatus.CLAIMED, device.getClaimStatus()),
                () -> assertEquals(ConnectionStatus.ONLINE, device.getConnectionStatus()),
                () -> assertEquals("redeem", device.getRedeemCode().toString()),
                () -> assertEquals(22.5, device.getMetrics().get(METRIC)));
    }

    @Test
    void bootValidatesIdentityAndMovesDeviceToPendingAndOnline() {
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        RandomGenerator generator = mock(RandomGenerator.class);
        when(encoder.matches(BOOTSTRAP, "bootstrap-hash")).thenReturn(true);
        when(encoder.matches("wrong", "bootstrap-hash")).thenReturn(false);
        when(generator.generateRedeemCode()).thenReturn("redeem");
        Device device = Device.create(SERIAL, "bootstrap-hash", "description", new Location(0, 0), List.of(METRIC));

        assertThrows(SecurityException.class, () -> device.boot("000000000000", BOOTSTRAP, generator, encoder));
        assertThrows(SecurityException.class, () -> device.boot(SERIAL, "", generator, encoder));
        assertThrows(SecurityException.class, () -> device.boot(SERIAL, "wrong", generator, encoder));

        RedeemCode code = device.boot(SERIAL, BOOTSTRAP, generator, encoder);

        assertEquals("redeem", code.toString());
        assertEquals(ClaimStatus.PENDING, device.getClaimStatus());
        assertEquals(ConnectionStatus.ONLINE, device.getConnectionStatus());
        assertEquals("bootstrap-hash", device.getBootstrapToken());
        assertThrows(SecurityException.class, () -> device.boot(SERIAL, BOOTSTRAP, generator, encoder));
    }

    @Test
    void bootAllowsPendingDeviceOnlyAfterItsRedeemCodeExpires() {
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        RandomGenerator generator = mock(RandomGenerator.class);
        when(encoder.matches(BOOTSTRAP, "bootstrap-hash")).thenReturn(true);
        when(generator.generateRedeemCode()).thenReturn("new-code");
        Device device = pendingDevice(RedeemCode.restore("old-code", Instant.now().minusSeconds(1),
                0, Instant.EPOCH, generator));

        assertEquals("new-code", device.boot(SERIAL, BOOTSTRAP, generator, encoder).toString());
        assertEquals(ClaimStatus.PENDING, device.getClaimStatus());
        assertEquals("new-code", device.getRedeemCode().toString());
    }

    @Test
    void claimRequiresBootAndValidRedeemCodeThenClearsIt() {
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        RandomGenerator generator = mock(RandomGenerator.class);
        Device unclaimed = Device.create(SERIAL, BOOTSTRAP, "description", new Location(0, 0), List.of(METRIC));
        assertThrows(SecurityException.class, () -> unclaimed.claim("owner", "redeem", generator, encoder));

        Device device = pendingDevice(RedeemCode.restore(
                "redeem", Instant.now().plusSeconds(60), 0, Instant.EPOCH, generator));

        assertThrows(InvalidCodeException.class, () -> device.claim("owner", "wrong", generator, encoder));
        assertNull(device.getOwnerId());
        assertEquals(ClaimStatus.PENDING, device.getClaimStatus());

        device.claim("owner", "redeem", generator, encoder);

        assertTrue(device.isClaimed());
        assertEquals("owner", device.getOwnerId());
        assertNull(device.getRedeemCode());
        assertNull(device.getCertificate());
        assertEquals(ConnectionStatus.ONLINE, device.getConnectionStatus());
        assertNotNull(device.getClaimedAt());
    }

    @Test
    void revokeAndMutatorsUpdateStateAndMetrics() {
        Device device = Device.restore(SERIAL, "owner", null, Instant.now(), ClaimStatus.CLAIMED,
                ConnectionStatus.ONLINE, new Location(0, 0), "old", Map.of(METRIC, 1.0),
                "bootstrap", null, Instant.now());
        PasswordEncoder encoder = mock(PasswordEncoder.class);
        when(encoder.matches("bootstrap", "bootstrap")).thenReturn(true);

        device.revoke();
        device.setLocation(new Location(5, 6));
        device.changeDescription("new");
        device.updateMetrics(Map.of(METRIC, 42.0, MetricsType.PRESSURE, 1000.0));

        assertAll(
                () -> assertEquals(ClaimStatus.REVOKED, device.getClaimStatus()),
                () -> assertEquals(ConnectionStatus.OFFLINE, device.getConnectionStatus()),
                () -> assertEquals(new Location(5, 6), device.getLocation()),
                () -> assertEquals("new", device.getDescription()),
                () -> assertEquals(42.0, device.getMetrics().get(METRIC)),
                () -> assertEquals(1000.0, device.getMetrics().get(MetricsType.PRESSURE)));
        assertThrows(SecurityException.class, device::revoke);
        assertThrows(SecurityException.class, () -> device.boot(
                SERIAL, "bootstrap", mock(RandomGenerator.class), encoder));
    }

    @Test
    void enrollmentRejectsRevokedDevicesAndDuplicateCertificates() {
        Device claimed = Device.restore(SERIAL, "owner", null, Instant.now(), ClaimStatus.CLAIMED,
                ConnectionStatus.ONLINE, new Location(0, 0), "description", Map.of(METRIC, 0.0),
                "bootstrap", null, Instant.now());
        Certificate certificate = new Certificate("pem", "serial", Instant.now().plusSeconds(120), Instant.now());

        claimed.enroll(certificate);
        assertEquals(certificate, claimed.getCertificate());
        assertThrows(SecurityException.class, () -> claimed.enroll(certificate));

        Device revoked = Device.restore(SERIAL, "owner", null, Instant.now(), ClaimStatus.REVOKED,
                ConnectionStatus.ONLINE, new Location(0, 0), "description", Map.of(METRIC, 0.0),
                "bootstrap", null, Instant.now());
        assertThrows(SecurityException.class, () -> revoked.enroll(certificate));
    }

    @Test
    void metricsGetterReturnsDefensiveCopy() {
        Device device = Device.create(SERIAL, BOOTSTRAP, "description", new Location(0, 0), List.of(METRIC));
        Map<MetricsType, Double> metrics = device.getMetrics();
        metrics.put(METRIC, 99.0);

        assertEquals(0.0, device.getMetrics().get(METRIC));
    }

    private Device pendingDevice(RedeemCode redeemCode) {
        return Device.restore(SERIAL, null, null, null, ClaimStatus.PENDING, ConnectionStatus.ONLINE,
                new Location(0, 0), "description", Map.of(METRIC, 0.0),
                "bootstrap-hash", redeemCode, Instant.now());
    }
}
