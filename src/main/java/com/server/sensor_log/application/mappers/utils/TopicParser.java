package com.server.sensor_log.application.mappers.utils;

import com.server.sensor_log.application.dto.TopicDTO;
import com.server.sensor_log.domain.model.device.ActionType;
import com.server.sensor_log.domain.model.device.DeviceType;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TopicParser {
    private static final Pattern TOPIC_PATTERN = Pattern.compile(
            "^iot/([a-z-]+)/([a-z-]+)/([a-z]+-\\d+)/(data|command)$"
    );

    public static TopicDTO parse(String topic) {
        Matcher matcher = TOPIC_PATTERN.matcher(topic);

        if (!matcher.matches()) {
            throw new IllegalArgumentException("Invalid topic: " + topic);
        }

        String location = matcher.group(1);
        String subLocation = matcher.group(2);
        String device = matcher.group(3);
        String action = matcher.group(4);

        String devicePrefix = device.split("-")[0];

        DeviceType deviceType = DeviceType.valueOf(
                devicePrefix.toUpperCase()
        );

        ActionType actionType = ActionType.valueOf(
                action.toUpperCase()
        );

        return new TopicDTO(
                device,
                deviceType,
                location,
                subLocation,
                actionType
        );
    }
}
