package com.server.sensor_log.dto;

import org.mapstruct.Mapper;

import com.server.sensor_log.documents.DeviceController;
import com.server.sensor_log.documents.Light;
import com.server.sensor_log.documents.Timer;

@Mapper(componentModel = "spring")
public interface LightMapper {

    LightPayloadDTO toDTO(Light light);

    Light toEntity(LightPayloadDTO dto);

    default Integer map(DeviceController value) {
        Integer valueFromDeviceController = value == null ? null : value.getValue();
        return valueFromDeviceController;
    }

    default DeviceController map(Integer value) {
        if (value == null) {
            return null;
        }

        DeviceController device = new DeviceController().setValue(value);
        return device;
    }

    default Timer map(Timer timer) {
        if (timer == null) {
            return null;
        }
        return new Timer(timer.getName(), timer.getHours(), timer.getMinutes(), timer.getIsActive());
    }
}
