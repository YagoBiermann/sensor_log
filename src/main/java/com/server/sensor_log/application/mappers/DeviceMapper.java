package com.server.sensor_log.application.mappers;

import com.server.sensor_log.application.dto.DeviceDTO;
import com.server.sensor_log.application.dto.TopicDTO;
import com.server.sensor_log.application.mappers.utils.TopicParser;
import com.server.sensor_log.domain.model.device.Device;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public abstract class DeviceMapper {

    @Mapping(target = "deviceId", source = "device.deviceId")
    @Mapping(target = "readingIds", source = "device.readingIds")
    @Mapping(target = "active", source = "device.active")
    @Mapping(target = "topic", expression = "java(TopicParser.buildTopic(device.getLocation(), device.getSubLocation(), device.getType()))")
    public abstract DeviceDTO toDTO(Device device);

    public Device toEntity(DeviceDTO dto) {
        if (dto == null) return null;
        
        TopicDTO topic = TopicParser.parse(dto.topic());
        
        return mapToEntity(dto, topic);
    }
    
    @Mapping(target = "deviceId", source = "dto.deviceId")
    @Mapping(target = "subLocation", source = "topic.subLocation")
    @Mapping(target = "type", source = "topic.deviceType")
    protected abstract Device mapToEntity(DeviceDTO dto, TopicDTO topic);

}
