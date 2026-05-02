package com.server.sensor_log.services;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.server.sensor_log.mqtt.MqttClientPort;
import com.server.sensor_log.mqtt.MqttMessageDispatcher;
import com.server.sensor_log.workers.ReconnectionWorker;

@ExtendWith(MockitoExtension.class)
class MqttServiceTest {

    @Mock
    private MqttClientPort mqttClient;
    @Mock
    private MqttMessageDispatcher dispatcher;
    @Mock
    private ReconnectionWorker reconnectionWorker;
    @InjectMocks
    private MqttService mqttService;

    // =========================
    // Helpers
    // =========================
    private Mqtt5Publish message(String topicStr, byte[] payload) {
        Mqtt5Publish message = mock(Mqtt5Publish.class);

        if (topicStr != null) {
            MqttTopic topic = mock(MqttTopic.class);
            when(topic.toString()).thenReturn(topicStr);
            when(message.getTopic()).thenReturn(topic);
        } else {
            when(message.getTopic()).thenReturn(null);
        }

        when(message.getPayloadAsBytes()).thenReturn(payload);

        return message;
    }

    private CompletableFuture<Void> successFuture() {
        return CompletableFuture.completedFuture(null);
    }

    private CompletableFuture<Void> failedFuture() {
        CompletableFuture<Void> future = new CompletableFuture<>();
        future.completeExceptionally(new RuntimeException());
        return future;
    }

    // =========================
    // Tests
    // =========================
    @Test
    void shouldConnectAndSubscribeOnStart() {
        mqttService.start();

        verify(mqttClient).connect();
        verify(mqttClient).subscribe(eq("iot/#"), any());
    }

    @Test
    void shouldReturnConnectionStatus() {
        when(mqttClient.isConnected()).thenReturn(true);
        assert mqttService.isConnected();

        when(mqttClient.isConnected()).thenReturn(false);
        assert !mqttService.isConnected();
    }

    @Test
    void shouldHandleReconnection() {
        doThrow(new RuntimeException("broker unavailable"))
                .when(mqttClient).connect();

        mqttService.start();

        verify(reconnectionWorker, times(1)).scheduleReconnect(any(Runnable.class));
        verify(mqttClient, never()).subscribe(any(), any());
    }

    @Test
    void shouldDisconnectOnStop() {
        mqttService.stop();

        verify(mqttClient).disconnect();
    }

    @Test
    void shouldPublishMessageSuccessfully() {
        when(mqttClient.publish(anyString(), anyString()))
                .thenReturn(successFuture());

        mqttService.publish("iot/test", "payload");

        verify(mqttClient).publish("iot/test", "payload");
    }

    @Test
    void shouldHandlePublishFailure() {
        when(mqttClient.publish(anyString(), anyString()))
                .thenReturn(failedFuture());

        mqttService.publish("iot/test", "payload");

        verify(mqttClient).publish("iot/test", "payload");
    }

    @Test
    void shouldSubscribeWhenConnectionSucceeds() {

        mqttService.start();

        verify(mqttClient).subscribe(eq("iot/#"), any());
        verifyNoInteractions(reconnectionWorker);
    }

    @Test
    void shouldSubscribeToNewTopic() {
        mqttService.subscribeToNewTopic("new/topic");

        verify(mqttClient).subscribe(eq("new/topic"), any());
    }

    @Test
    void shouldHandleMessageAndDispatchCorrectly() {
        var msg = message("iot/temp", "25C".getBytes());

        mqttService.handleMessage(msg);

        verify(dispatcher).dispatch("iot/temp", "25C");
    }

    @Test
    void shouldNotThrowIfDispatcherFails() {
        var msg = message("iot/temp", "error".getBytes());

        doThrow(new RuntimeException())
                .when(dispatcher).dispatch(anyString(), anyString());

        assertDoesNotThrow(() -> mqttService.handleMessage(msg));

        verify(dispatcher).dispatch("iot/temp", "error");
    }

    @Test
    void shouldHandleInvalidTopic() {
        var msg = message("", "25".getBytes());

        assertDoesNotThrow(() -> mqttService.handleMessage(msg));

        verify(dispatcher, never()).dispatch(any(), any());
    }

    @Test
    void shouldHandleInvalidPayload() {
        var msg = message("iot/temp", new byte[]{(byte) 0xFF});

        assertDoesNotThrow(() -> mqttService.handleMessage(msg));

        verify(dispatcher).dispatch(eq("iot/temp"), anyString());
    }

    @Test
    void shouldUpdateTopicAndSubscribe() {
        String topic = "iot/test";

        mqttService.subscribeToNewTopic(topic);

        assertEquals(topic, mqttService.getTopic());

        @SuppressWarnings("unchecked")
        var captor = ArgumentCaptor.forClass(Consumer.class);

        verify(mqttClient).subscribe(eq(topic), captor.capture());

        Consumer<Mqtt5Publish> callback = captor.getValue();
        assert callback != null;
    }
}
