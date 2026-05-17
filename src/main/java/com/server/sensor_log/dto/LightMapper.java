package com.server.sensor_log.dto;

import org.mapstruct.Builder;
import org.mapstruct.Mapper;

import com.server.sensor_log.documents.Light;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", builder = @Builder(disableBuilder = false), unmappedTargetPolicy = ReportingPolicy.WARN)
public interface LightMapper {

    LightPayloadDTO toDTO(Light light);

    Light toEntity(LightPayloadDTO dto);
}
