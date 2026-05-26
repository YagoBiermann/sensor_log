package com.server.sensor_log.application.mappers;

import com.server.sensor_log.application.dto.LightReadingDTO;
import com.server.sensor_log.domain.model.device.device_readings.LightReading;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = false), unmappedTargetPolicy = ReportingPolicy.WARN)
public interface LightMapper {

    LightReadingDTO toDTO(LightReading lightReading);

    LightReading toEntity(LightReadingDTO dto);
}
