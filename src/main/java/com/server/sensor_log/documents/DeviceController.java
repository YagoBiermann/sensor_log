package com.server.sensor_log.documents;

public class DeviceController {

    private Integer value;

    public DeviceController() {
        this.value = 0;
    }

    public void setValue(Integer value) {
        if (value < 0 || value > 100) {
            throw new IllegalArgumentException("Value must be between 0 and 100");
        }
        this.value = value;
    }

    public Integer getValue() {
        return value;
    }
}
