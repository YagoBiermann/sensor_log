package com.server.sensor_log.application.dto;

import com.server.sensor_log.domain.model.device.Device;
import org.mapstruct.Builder;
import org.mapstruct.Mapper;

import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = false), unmappedTargetPolicy = ReportingPolicy.WARN)
public interface DeviceMapper {

    DeviceDTO toDTO(Device device);

    Device toEntity(DeviceDTO dto);
}
