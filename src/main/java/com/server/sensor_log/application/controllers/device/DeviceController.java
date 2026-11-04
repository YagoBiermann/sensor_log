package com.server.sensor_log.application.controllers.device;

import java.security.cert.CertificateEncodingException;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.server.sensor_log.application.controllers.device.dto.ClaimDeviceRequest;
import com.server.sensor_log.application.controllers.device.dto.ClaimDeviceRequestBody;
import com.server.sensor_log.application.controllers.device.dto.ClaimDeviceResponse;
import com.server.sensor_log.application.controllers.device.dto.CreateDeviceRequest;
import com.server.sensor_log.application.controllers.device.dto.CreateDeviceResponse;
import com.server.sensor_log.application.controllers.device.dto.DeviceResponse;
import com.server.sensor_log.application.controllers.device.dto.FirstBootRequest;
import com.server.sensor_log.application.controllers.device.dto.FirstBootResponse;
import com.server.sensor_log.application.controllers.device.dto.NewDevice;
import com.server.sensor_log.application.exceptions.DeviceNotFoundException;
import com.server.sensor_log.application.services.JwtService;
import com.server.sensor_log.application.services.MqttDynamicSecurityService;
import com.server.sensor_log.application.usecases.BootDeviceUseCase;
import com.server.sensor_log.application.usecases.ClaimDeviceUseCase;
import com.server.sensor_log.application.usecases.CreateDeviceUseCase;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.infra.repository.DeviceRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {
    private final ClaimDeviceUseCase claimDeviceUseCase;
    private final CreateDeviceUseCase createDeviceUseCase;
    private final BootDeviceUseCase bootDeviceUseCase;
    private final JwtService jwtService;
    private final MqttDynamicSecurityService mqttDynamicSecurityService;
    private final DeviceRepository deviceRepository;

    public DeviceController(JwtService jwtService, ClaimDeviceUseCase claimDeviceUseCase,
            CreateDeviceUseCase createDeviceUseCase,
            BootDeviceUseCase bootDeviceUseCase,
            MqttDynamicSecurityService mqttDynamicSecurityService,
            DeviceRepository deviceRepository) {
        this.jwtService = jwtService;
        this.claimDeviceUseCase = claimDeviceUseCase;
        this.createDeviceUseCase = createDeviceUseCase;
        this.bootDeviceUseCase = bootDeviceUseCase;
        this.mqttDynamicSecurityService = mqttDynamicSecurityService;
        this.deviceRepository = deviceRepository;
    }

    @PostMapping("/{serial}/claim")
    public ResponseEntity<?> ClaimDevice(@PathVariable String serial,
            @Valid @RequestBody ClaimDeviceRequestBody body,
            @RequestHeader("Authorization") String authorization) {
        try {
            String userId = jwtService.extractUserId(extractBearerToken(authorization));
            ClaimDeviceRequest request = new ClaimDeviceRequest(serial, userId, body.redeemCode(), body.csr());
            ClaimDeviceResponse response = claimDeviceUseCase.execute(request);
            return ResponseEntity.ok(response);
        } catch (DeviceNotFoundException | SecurityException | CertificateEncodingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{serial}/boot")
    public ResponseEntity<FirstBootResponse> BootDevice(@PathVariable String serial,
            @RequestBody String firstBootToken) {
        try {
            FirstBootRequest firstBootRequest = new FirstBootRequest(serial, firstBootToken);
            FirstBootResponse response = bootDeviceUseCase.execute(firstBootRequest);

            return ResponseEntity.ok(response);
        } catch (DeviceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    @PostMapping("/devices/{serial}/revoke")
    public ResponseEntity<String> RevokeDevice(@PathVariable String serial, @RequestHeader("Bearer") String token) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping("/devices/{serial}")
    public ResponseEntity<String> GetDevice(@RequestHeader("Bearer") String token, @PathVariable String serial) {
        return ResponseEntity.ok("Device information for serial: " + serial);
    }

    @PostMapping
    public ResponseEntity<CreateDeviceResponse> CreateDevice(@RequestHeader("Bearer") String token,
            @RequestBody CreateDeviceRequest createDeviceRequest) {
        NewDevice newDevice = new NewDevice(createDeviceRequest.deviceMetrics());
        CreateDeviceResponse response = createDeviceUseCase.execute(newDevice);
        mqttDynamicSecurityService.createDeviceClient(response.serialNumber()).join();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/devices/{serial}/metrics")
    public ResponseEntity<String> GetDeviceMetrics(@PathVariable String serial) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping("/metrics")
    public ResponseEntity<String> GetAllMetrics(@RequestHeader("Bearer") String token) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
