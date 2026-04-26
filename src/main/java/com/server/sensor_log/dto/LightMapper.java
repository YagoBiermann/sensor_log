package com.server.sensor_log.dto;

import org.mapstruct.Mapper;

import com.server.sensor_log.documents.DeviceController;
import com.server.sensor_log.documents.Light;

@Mapper(componentModel = "spring")
public interface LightMapper {

    LightPayloadDTO toDTO(Light light);

    Light toEntity(LightPayloadDTO dto);

    default Integer map(DeviceController value) {
        return value == null ? null : value.getValue();
    }

    default DeviceController map(Integer value) {
        if (value == null) {
            return null;
        }
        DeviceController device = new DeviceController();
        device.setValue(value);
        return device;
    }
}
