package com.server.sensor_log.dto;

import org.mapstruct.Mapper;

import com.server.sensor_log.documents.Light;

@Mapper(componentModel = "spring")
public interface LightMapper {

    LightPayloadDTO toDTO(Light light);

    Light toEntity(LightPayloadDTO dto);
}
