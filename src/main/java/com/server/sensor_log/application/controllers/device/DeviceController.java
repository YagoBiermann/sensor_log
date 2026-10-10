package com.server.sensor_log.application.controllers.device;

import java.security.cert.CertificateEncodingException;

import javax.naming.NameNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.server.sensor_log.application.controllers.device.dto.FirstBootResponse;
import com.server.sensor_log.application.controllers.device.dto.CertificateResponse;
import com.server.sensor_log.application.controllers.device.dto.ClaimDeviceResponse;
import com.server.sensor_log.application.controllers.device.dto.CreateDeviceRequest;
import com.server.sensor_log.application.controllers.device.dto.CreateDeviceResponse;
import com.server.sensor_log.application.controllers.device.dto.FirstBootRequest;
import com.server.sensor_log.application.controllers.device.dto.ClaimDeviceRequest;
import com.server.sensor_log.application.controllers.device.dto.ClaimDeviceRequestBody;
import com.server.sensor_log.application.exceptions.DeviceNotFoundException;
import com.server.sensor_log.application.services.AuthenticationService;
import com.server.sensor_log.application.services.JwtService;
import com.server.sensor_log.application.usecases.BootDeviceUseCase;
import com.server.sensor_log.application.usecases.ClaimDeviceUseCase;
import com.server.sensor_log.application.usecases.CreateDeviceUseCase;
import com.server.sensor_log.application.usecases.dto.NewDevice;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {
    private final ClaimDeviceUseCase claimDeviceUseCase;
    private final CreateDeviceUseCase createDeviceUseCase;
    private final BootDeviceUseCase bootDeviceUseCase;
    private final JwtService jwtService;

    public DeviceController(JwtService jwtService, ClaimDeviceUseCase claimDeviceUseCase,
            CreateDeviceUseCase createDeviceUseCase,
            BootDeviceUseCase bootDeviceUseCase, AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.claimDeviceUseCase = claimDeviceUseCase;
        this.createDeviceUseCase = createDeviceUseCase;
        this.bootDeviceUseCase = bootDeviceUseCase;
    }

    @PostMapping("/devices/{serial}/claim")
    public ResponseEntity<?> ClaimDevice(@PathVariable String serial,
            @RequestBody ClaimDeviceRequestBody body,
            @RequestHeader("Bearer") String token) {
        try {
            String userId = jwtService.extractUserId(token);
            ClaimDeviceRequest request = new ClaimDeviceRequest(serial, userId, body.redeemCode(), body.csr());
            ClaimDeviceResponse response = claimDeviceUseCase.execute(request);
            return ResponseEntity.ok(response);
        } catch (DeviceNotFoundException | SecurityException | CertificateEncodingException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/devices/{serial}/boot")
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
        NewDevice newDevice = new NewDevice(createDeviceRequest.description(), createDeviceRequest.location(),
                createDeviceRequest.deviceMetrics());
        CreateDeviceResponse response = createDeviceUseCase.execute(newDevice);

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
