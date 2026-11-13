package com.server.sensor_log.application.controllers.device;

import java.net.URI;
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
import com.server.sensor_log.application.controllers.device.dto.CertificateResponse;
import com.server.sensor_log.application.controllers.device.dto.CreateDeviceRequest;
import com.server.sensor_log.application.controllers.device.dto.CreateDeviceResponse;
import com.server.sensor_log.application.controllers.device.dto.DeviceEnrollmentRequestBody;
import com.server.sensor_log.application.controllers.device.dto.DeviceResponse;
import com.server.sensor_log.application.controllers.device.dto.FirstBootRequest;
import com.server.sensor_log.application.controllers.device.dto.FirstBootResponse;
import com.server.sensor_log.application.controllers.device.dto.NewDevice;
import com.server.sensor_log.application.controllers.device.dto.RedeemCodeResponse;
import com.server.sensor_log.application.exceptions.InvalidDeviceStatusException;
import com.server.sensor_log.application.exceptions.DeviceNotFoundException;
import com.server.sensor_log.application.exceptions.ExpiredCodeException;
import com.server.sensor_log.application.exceptions.InvalidCodeException;
import com.server.sensor_log.application.exceptions.MaxAttemptsFailedException;
import com.server.sensor_log.application.exceptions.RetryBeforeCooldownException;
import com.server.sensor_log.application.services.AuthenticationService;
import com.server.sensor_log.application.services.JwtService;
import com.server.sensor_log.application.services.MqttDynamicSecurityService;
import com.server.sensor_log.application.usecases.BootDeviceUseCase;
import com.server.sensor_log.application.usecases.ClaimDeviceUseCase;
import com.server.sensor_log.application.usecases.CreateDeviceUseCase;
import com.server.sensor_log.application.usecases.DeviceEnrollmentUseCase;
import com.server.sensor_log.application.usecases.RevokeDeviceUseCase;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.domain.model.device.Role;
import com.server.sensor_log.infra.repository.DeviceRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {
    private final ClaimDeviceUseCase claimDeviceUseCase;
    private final CreateDeviceUseCase createDeviceUseCase;
    private final BootDeviceUseCase bootDeviceUseCase;
    private final JwtService jwtService;
    private final AuthenticationService authenticationService;
    private final RevokeDeviceUseCase revokeDeviceUseCase;
    private final DeviceEnrollmentUseCase deviceEnrollmentUseCase;
    private final MqttDynamicSecurityService mqttDynamicSecurityService;
    private final DeviceRepository deviceRepository;

    public DeviceController(JwtService jwtService,
            AuthenticationService authenticationService,
            ClaimDeviceUseCase claimDeviceUseCase,
            CreateDeviceUseCase createDeviceUseCase,
            BootDeviceUseCase bootDeviceUseCase,
            RevokeDeviceUseCase revokeDeviceUseCase,
            DeviceEnrollmentUseCase deviceEnrollmentUseCase,
            MqttDynamicSecurityService mqttDynamicSecurityService,
            DeviceRepository deviceRepository) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.claimDeviceUseCase = claimDeviceUseCase;
        this.createDeviceUseCase = createDeviceUseCase;
        this.bootDeviceUseCase = bootDeviceUseCase;
        this.revokeDeviceUseCase = revokeDeviceUseCase;
        this.deviceEnrollmentUseCase = deviceEnrollmentUseCase;
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
        } catch (DeviceNotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        } catch (MaxAttemptsFailedException | RetryBeforeCooldownException e) {
            return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(e.getMessage());
        } catch (ExpiredCodeException e) {
            return ResponseEntity.status(HttpStatus.GONE).body(e.getMessage());
        } catch (InvalidCodeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    @PostMapping("/{serial}/enrollment")
    public ResponseEntity<CertificateResponse> EnrollDevice(
            @PathVariable String serial,
            @Valid @RequestBody DeviceEnrollmentRequestBody body) throws CertificateEncodingException {
        try {
            return ResponseEntity.ok(deviceEnrollmentUseCase.execute(serial, body.csr()));
        } catch (DeviceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (InvalidDeviceStatusException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        } catch (ExpiredCodeException e) {
            return ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create("/api/devices/" + serial + "/code"))
                    .build();
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

    @PostMapping("/{serial}/revoke")
    public ResponseEntity<?> RevokeDevice(@PathVariable String serial, @RequestHeader("Authorization") String authorization) {
        try {
            String token = extractBearerToken(authorization);
            revokeDeviceUseCase.execute(
                    serial,
                    jwtService.extractUserId(token),
                    jwtService.extractRole(token));
            return ResponseEntity.noContent().build();
        } catch (DeviceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/{serial}")
    public ResponseEntity<?> GetDevice(@RequestHeader("Authorization") String authorization, @PathVariable String serial) {
        try {
            String token = extractBearerToken(authorization);
            var device = deviceRepository.findById(serial).orElseThrow(() -> new DeviceNotFoundException("Device not found"));
            authenticationService.authorizeOwnerOrAdmin(
                    device.getOwnerId(),
                    jwtService.extractUserId(token),
                    jwtService.extractRole(token));
            return ResponseEntity.ok(toResponse(device));
        } catch (DeviceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @GetMapping("/{serial}/code")
    public ResponseEntity<?> GetDeviceCode(
            @PathVariable String serial) {
        try {
            Device device = deviceRepository.findById(serial)
                    .orElseThrow(() -> new DeviceNotFoundException("Device not found"));

            if (device.getRedeemCode() == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(new RedeemCodeResponse(device.getRedeemCode().toString()));
        } catch (DeviceNotFoundException e) {
            return ResponseEntity.notFound().build();
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    @PostMapping
    public ResponseEntity<CreateDeviceResponse> CreateDevice(@Valid @RequestBody CreateDeviceRequest createDeviceRequest) {
        NewDevice newDevice = new NewDevice(createDeviceRequest.deviceMetrics());
        CreateDeviceResponse response = createDeviceUseCase.execute(newDevice);
        mqttDynamicSecurityService.createDeviceClient(response.serialNumber()).join();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{serial}/metrics")
    public ResponseEntity<?> GetDeviceMetrics(@PathVariable String serial, @RequestHeader("Authorization") String authorization) {
        return GetDevice(authorization, serial);
    }

    @GetMapping("/metrics")
    public ResponseEntity<?> GetAllMetrics(@RequestHeader("Authorization") String authorization) {
        try {
            String token = extractBearerToken(authorization);
            String userId = jwtService.extractUserId(token);
            boolean admin = authenticationService.isAdmin(jwtService.extractRole(token));
            List<DeviceResponse> devices = deviceRepository.findAll().stream()
                    .filter(device -> admin || userId.equals(device.getOwnerId()))
                    .map(this::toResponse)
                    .collect(Collectors.toList());
            return ResponseEntity.ok(devices);
        } catch (SecurityException e) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(e.getMessage());
        }
    }

    private String extractBearerToken(String authorization) {
        if (authorization == null || !authorization.startsWith("Bearer ")) {
            throw new SecurityException("Invalid Authorization header");
        }
        return authorization.substring(7);
    }

    private DeviceResponse toResponse(Device device) {
        return new DeviceResponse(device.getSerialNumber(), device.getOwnerId(), device.getClaimStatus(),device.getLocation(),
                device.getConnectionStatus(), device.getCreatedAt(), device.getMetrics());
    }
}
