package com.server.sensor_log.application.mappers;

import com.server.sensor_log.application.dto.DeviceDTO;
import com.server.sensor_log.application.dto.TopicDTO;
import com.server.sensor_log.application.mappers.utils.TopicParser;
import com.server.sensor_log.domain.model.device.Device;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.WARN)
public abstract class DeviceMapper {

    public abstract DeviceDTO toDTO(Device device);

    public Device toEntity(DeviceDTO dto) {
        if (dto == null) return null;

        TopicDTO topic = TopicParser.parse(dto.topic());

        return mapToEntity(dto, topic);
    }

    @Mapping(target = "subLocation", source = "topic.subLocation")
    @Mapping(target = "type", source = "topic.deviceType")
    protected abstract Device mapToEntity(DeviceDTO dto, TopicDTO topic);

}
