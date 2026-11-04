package com.server.sensor_log.application.services;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.concurrent.CompletableFuture;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.sensor_log.infra.messaging.mqtt.MqttConnectionManager;

@ExtendWith(MockitoExtension.class)
class MqttDynamicSecurityServiceTest {
    @Mock MqttConnectionManager connectionManager;

    @Test
    void publishesValidDeviceCommand() throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        when(connectionManager.publish(anyString(), anyString())).thenReturn(CompletableFuture.completedFuture(null));
        var service = new MqttDynamicSecurityService(connectionManager, mapper);

        service.createDeviceClient("123456789012").join();

        verify(connectionManager).publish(eq("$CONTROL/dynamic-security/v1"), argThat(payload ->
                payload.contains("\"username\":\"123456789012\"")
                        && payload.contains("iot/%u/data")
                        && payload.contains("iot/%u/command")));
    }

    @Test
    void rejectsInvalidSerial() {
        var service = new MqttDynamicSecurityService(connectionManager, new ObjectMapper());
        assertThrows(IllegalArgumentException.class, () -> service.createDeviceClient("invalid"));
        verifyNoInteractions(connectionManager);
    }
}
