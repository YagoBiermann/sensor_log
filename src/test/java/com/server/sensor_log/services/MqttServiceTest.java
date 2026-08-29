package com.server.sensor_log.services;

import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

import com.server.sensor_log.infra.messaging.mqtt.MqttConnectionManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.hivemq.client.mqtt.datatypes.MqttTopic;
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish;
import com.server.sensor_log.infra.messaging.mqtt.MqttClientPort;
import com.server.sensor_log.infra.messaging.mqtt.MqttMessageDispatcher;
import com.server.sensor_log.application.workers.ReconnectionWorker;

@ExtendWith(MockitoExtension.class)
class MqttServiceTest {

    @Mock
    private MqttClientPort mqttClient;
    @Mock
    private MqttMessageDispatcher dispatcher;
    @Mock
    private ReconnectionWorker reconnectionWorker;
    @InjectMocks
    private MqttConnectionManager mqttConnectionManager;

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

    @BeforeEach
    void setUp() {
        lenient().when(mqttClient.connect())
                .thenReturn(CompletableFuture.completedFuture(null));
        lenient().when(mqttClient.disconnect())
                .thenReturn(CompletableFuture.completedFuture(null));
        lenient().when(mqttClient.reconnect(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
        lenient().when(mqttClient.subscribe(any(), any()))
                .thenReturn(CompletableFuture.completedFuture(null));
    }

    // =========================
    // Tests
    // =========================
    @Test
    void shouldConnectAndSubscribeOnStart() {
        mqttConnectionManager.start();

        verify(mqttClient).connect();
        verify(mqttClient).subscribe(eq("iot/#"), any());
        verifyNoInteractions(reconnectionWorker);
    }

    @Test
    void shouldHandleReconnection() {
        when(mqttClient.connect())
                .thenReturn(CompletableFuture.failedFuture(
                        new RuntimeException("broker unavailable")));

        mqttConnectionManager.start();

        verify(reconnectionWorker, times(1)).scheduleReconnect(any(Runnable.class));
        verify(mqttClient, never()).subscribe(any(), any());
    }

    @Test
    void shouldDisconnectOnStop() {
        mqttConnectionManager.stop();

        verify(mqttClient).disconnect();
    }

    @Test
    void shouldPublishMessageSuccessfully() {
        when(mqttClient.publish(anyString(), anyString()))
                .thenReturn(successFuture());

        mqttConnectionManager.publish("iot/test", "payload");

        verify(mqttClient).publish("iot/test", "payload");
    }

    @Test
    void shouldHandlePublishFailure() {
        when(mqttClient.publish(anyString(), anyString()))
                .thenReturn(failedFuture());

        mqttConnectionManager.publish("iot/test", "payload");

        verify(mqttClient).publish("iot/test", "payload");
    }

    @Test
    void shouldSubscribeToNewTopic() {
        mqttConnectionManager.subscribe("new/topic");

        verify(mqttClient).subscribe(eq("new/topic"), any());
    }

    @Test
    void shouldHandleMessageAndDispatchCorrectly() {
        var msg = message("iot/temp", "25C".getBytes());

        mqttConnectionManager.handleMessage(msg);

        verify(dispatcher).dispatch("iot/temp", "25C");
    }

    @Test
    void shouldNotThrowIfDispatcherFails() {
        var msg = message("iot/temp", "error".getBytes());

        doThrow(new RuntimeException())
                .when(dispatcher).dispatch(anyString(), anyString());

        assertDoesNotThrow(() -> mqttConnectionManager.handleMessage(msg));

        verify(dispatcher).dispatch("iot/temp", "error");
    }

    @Test
    void shouldHandleInvalidTopic() {
        var msg = message("", "25".getBytes());

        assertDoesNotThrow(() -> mqttConnectionManager.handleMessage(msg));

        verify(dispatcher, never()).dispatch(any(), any());
    }

    @Test
    void shouldHandleInvalidPayload() {
        var msg = message("iot/temp", new byte[]{(byte) 0xFF});

        assertDoesNotThrow(() -> mqttConnectionManager.handleMessage(msg));

        verify(dispatcher, never()).dispatch(eq("iot/temp"), anyString());
    }

    @Test
    void shouldUpdateTopicAndSubscribe() {
        String topic = "iot/test";

        mqttConnectionManager.subscribe(topic);

        assertEquals(topic, mqttConnectionManager.getTopic());

        @SuppressWarnings("unchecked")
        var captor = ArgumentCaptor.forClass(Consumer.class);

        verify(mqttClient).subscribe(eq(topic), captor.capture());

        Consumer<Mqtt5Publish> callback = captor.getValue();
        assert callback != null;
    }

    @Test
    void shouldRejectBlankPayload() {
        var msg = message("iot/temp", new byte[0]); // or "   ".getBytes()
        assertDoesNotThrow(() -> mqttConnectionManager.handleMessage(msg));
        verify(dispatcher, never()).dispatch(any(), any());
    }

    @Test
    void shouldCallReconnectOnScheduledRunnable() {
        when(mqttClient.connect())
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("down")));

        mqttConnectionManager.start();

        var captor = ArgumentCaptor.forClass(Runnable.class);
        verify(reconnectionWorker).scheduleReconnect(captor.capture());

        captor.getValue().run();

        verify(mqttClient).reconnect(any(), any());
    }

    @Test
    void shouldCancelReconnectWorkerOnSuccessfulReconnect() {
        when(mqttClient.connect())
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("down")));

        mqttConnectionManager.start();

        var captor = ArgumentCaptor.forClass(Runnable.class);
        verify(reconnectionWorker).scheduleReconnect(captor.capture());
        captor.getValue().run();

        verify(reconnectionWorker).cancelReconnect();
    }

    @Test
    void shouldNotCancelReconnectWorkerOnFailedReconnect() {
        when(mqttClient.connect())
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("down")));
        when(mqttClient.reconnect(any(), any()))
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("still down")));

        mqttConnectionManager.start();

        var captor = ArgumentCaptor.forClass(Runnable.class);
        verify(reconnectionWorker).scheduleReconnect(captor.capture());
        captor.getValue().run();

        verify(reconnectionWorker, never()).cancelReconnect();
    }

    @Test
    void shouldHandleDisconnectFailureGracefully() {
        when(mqttClient.disconnect())
                .thenReturn(CompletableFuture.failedFuture(new RuntimeException("disconnect failed")));

        assertDoesNotThrow(() -> mqttConnectionManager.stop());
    }

    @Test
    void shouldHaveDefaultTopic() {
        assertEquals("iot/#", mqttConnectionManager.getTopic());
    }
}
