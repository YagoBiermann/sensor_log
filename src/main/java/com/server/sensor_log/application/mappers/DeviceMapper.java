package com.server.sensor_log.application.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.ReportingPolicy;

import com.server.sensor_log.application.dto.DeviceDTO;
import com.server.sensor_log.application.dto.TopicDTO;
import com.server.sensor_log.application.mappers.utils.TopicParser;
import com.server.sensor_log.domain.model.device.Device;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN, imports = TopicParser.class)
public abstract class DeviceMapper {

    @Mapping(target = "deviceId", source = "device.deviceId")
    @Mapping(target = "readingIds", source = "device.readingIds")
    @Mapping(target = "active", source = "device.active")
    @Mapping(target = "topic", source = "device", qualifiedByName = "toTopic")
    public abstract DeviceDTO toDTO(Device device);

    public Device toEntity(DeviceDTO dto) {
        if (dto == null) {
            return null;
        }

        TopicDTO topic = TopicParser.parse(dto.getTopic());

        return mapToEntity(dto, topic);
    }

    @Mapping(target = "deviceId", source = "dto.deviceId")
    @Mapping(target = "readingIds", source = "dto.readingIds")
    @Mapping(target = "timer", source = "dto.timer")
    @Mapping(target = "active", source = "dto.active")
    @Mapping(target = "location", source = "topic.location")
    @Mapping(target = "subLocation", source = "topic.subLocation")
    @Mapping(target = "type", source = "topic.deviceType")
    protected abstract Device mapToEntity(DeviceDTO dto, TopicDTO topic);


    @Named("toTopic")
    String toTopic(Device device) {
        if (device == null) {
            return null;
        }
        return device.getTopic();
    }
}
