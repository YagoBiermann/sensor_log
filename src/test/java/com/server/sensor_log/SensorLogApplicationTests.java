package com.server.sensor_log;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.server.sensor_log.services.MqttService;

@SpringBootTest()
@ActiveProfiles("test")
class SensorLogApplicationTests {

    @MockitoBean
    private MqttService mqttService;

    @Test
    void contextLoads() {

    }
}
