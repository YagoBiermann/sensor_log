package com.server.sensor_log.application.services;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.server.sensor_log.infra.messaging.mqtt.MqttConnectionManager;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MqttDynamicSecurityService {

    private static final String CONTROL_TOPIC = "$CONTROL/dynamic-security/v1";
    private static final String DEVICE_ROLE = "DEVICE";

    private final MqttConnectionManager mqttConnectionManager;
    private final ObjectMapper objectMapper;

    public CompletableFuture<Void> createDeviceClient(String serialNumber) {
        validateSerialNumber(serialNumber);

        try {
            return mqttConnectionManager.publish(CONTROL_TOPIC, commandPayload(serialNumber));
        } catch (JsonProcessingException exception) {
            return CompletableFuture.failedFuture(exception);
        }
    }

    private String commandPayload(String serialNumber) throws JsonProcessingException {
        Map<String, Object> createRole = Map.of(
                "command", "createRole",
                "rolename", DEVICE_ROLE,
                "acls", List.of(
                        acl("publishClientSend", "iot/%u/+/data"),
                        acl("subscribePattern", "iot/%u/+/command"),
                        acl("publishClientReceive", "iot/%u/+/command")));

        Map<String, Object> createClient = Map.of(
                "command", "createClient",
                "username", serialNumber,
                "roles", List.of(Map.of(
                        "rolename", DEVICE_ROLE,
                        "priority", -1)));

        return objectMapper.writeValueAsString(Map.of(
                "commands", List.of(createRole, createClient)));
    }

    private Map<String, Object> acl(String aclType, String topic) {
        return Map.of(
                "acltype", aclType,
                "topic", topic,
                "priority", -1,
                "allow", true);
    }

    private void validateSerialNumber(String serialNumber) {
        if (serialNumber == null || !serialNumber.matches("[A-Za-z0-9]{12}")) {
            throw new IllegalArgumentException("Invalid device serial number");
        }
    }
}
