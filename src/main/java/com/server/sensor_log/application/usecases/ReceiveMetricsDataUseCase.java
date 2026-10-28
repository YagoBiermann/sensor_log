package com.server.sensor_log.application.usecases;

import com.server.sensor_log.application.ports.Payload;
import com.server.sensor_log.application.ports.Topic;

//TODO add device validation, check if device exists, check if metrics are valid for the device, etc.
public class ReceiveMetricsDataUseCase {
    public void execute(Topic topic, Payload payload) {
        System.out.println("Received metrics data for topic: " + topic + ", payload: " + payload);
    }
}
