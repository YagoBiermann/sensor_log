package com.server.sensor_log.application.usecases;

import org.springframework.stereotype.Component;

import com.server.sensor_log.application.dto.LightReadingDTO;
import com.server.sensor_log.application.mappers.LightMapper;
import com.server.sensor_log.application.ports.LightRepositoryPort;
import com.server.sensor_log.domain.model.device.device_readings.LightReading;

import lombok.extern.slf4j.Slf4j;

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
        boolean foundLightDevice = lightRepositoryPort.findById(reading.getDeviceId()).isPresent();
        if(!foundLightDevice) {
            log.warn("🟠 Could not find Light device: {}", reading.getDeviceId());
            return;
        }
        log.info("🔵 Saving light readings: {}", reading);
        LightReading lightReading = lightMapper.toEntity(reading);
        log.debug("🔵 Mapped LightReadingDTO to Light entity: {}", lightReading);
        lightRepositoryPort.save(lightReading);
        log.info("🟢 saved light readings successfully: {}", lightReading);
    }
}
