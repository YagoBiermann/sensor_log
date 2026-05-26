package com.server.sensor_log.application.usecases;

import com.server.sensor_log.application.ports.LightRepositoryPort;
import com.server.sensor_log.domain.model.device.device_readings.LightReading;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SaveLightReadingUseCase {
    private final LightRepositoryPort lightRepositoryPort;

    public SaveLightReadingUseCase(LightRepositoryPort lightRepositoryPort) {
        this.lightRepositoryPort = lightRepositoryPort;
    }

    public void execute(LightReading reading) {
        boolean foundLightDevice = lightRepositoryPort.findById(reading.getId()).isPresent();
        if(!foundLightDevice) {
            throw new RuntimeException("Could not find Light device: %s".formatted(reading.getId()));
        }
        log.debug("🔵 Saving light readings: {}", reading);
        lightRepositoryPort.save(reading);
    }
}
