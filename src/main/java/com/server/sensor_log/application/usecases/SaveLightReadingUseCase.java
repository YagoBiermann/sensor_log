package com.server.sensor_log.application.usecases;

import com.server.sensor_log.application.dto.LightMapper;
import com.server.sensor_log.application.dto.LightReadingDTO;
import com.server.sensor_log.application.ports.LightRepositoryPort;
import com.server.sensor_log.domain.model.device.device_readings.LightReading;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SaveLightReadingUseCase {
    private final LightRepositoryPort lightRepositoryPort;
    private final LightMapper lightMapper;

    public SaveLightReadingUseCase(LightRepositoryPort lightRepositoryPort, LightMapper lightMapper) {
        this.lightRepositoryPort = lightRepositoryPort;
        this.lightMapper = lightMapper;
    }

    public void execute(LightReadingDTO reading) {
        boolean foundLightDevice = lightRepositoryPort.findById(reading.getId()).isPresent();
        if(!foundLightDevice) {
            throw new RuntimeException("Could not find Light device: %s".formatted(reading.getId()));
        }
        log.debug("🔵 Saving light readings: {}", reading);
        LightReading lightReading = lightMapper.toEntity(reading);
        log.debug("🔵 Mapped LightPayloadDTO to Light entity: {}", lightReading);
        lightRepositoryPort.save(lightReading);
    }
}
