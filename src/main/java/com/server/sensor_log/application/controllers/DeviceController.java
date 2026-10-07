package com.server.sensor_log.application.controllers;

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

import com.server.sensor_log.application.controllers.dto.CertificateResponse;
import com.server.sensor_log.application.controllers.dto.CreateDeviceRequest;
import com.server.sensor_log.application.controllers.dto.CreateDeviceResponse;
import com.server.sensor_log.application.controllers.dto.SignCertificateRequest;
import com.server.sensor_log.application.services.AuthenticationService;
import com.server.sensor_log.application.services.JwtService;
import com.server.sensor_log.application.usecases.CreateDeviceUseCase;
import com.server.sensor_log.application.usecases.dto.NewDevice;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/devices")
public class DeviceController {
    private final JwtService jwtService;
    private final CreateDeviceUseCase createDeviceUseCase;
    private final AuthenticationService authenticationService;

    public DeviceController(JwtService jwtService, CreateDeviceUseCase createDeviceUseCase,
            AuthenticationService authenticationService) {
        this.jwtService = jwtService;
        this.createDeviceUseCase = createDeviceUseCase;
        this.authenticationService = authenticationService;
    }

    @PostMapping("/devices/{id}/claim")
    public ResponseEntity<String> ClaimDevice(@PathVariable String id, @RequestBody String claimCode,
            @RequestHeader("Bearer") String token) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    // TODO: Definir dispositivo como online se bootstrap token válido.
    @PostMapping("/devices/{id}/boot")
    public ResponseEntity<String> BootDevice(@PathVariable String id,
            @RequestHeader("Bearer") @RequestBody String firstBootToken) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/devices/{id}/revoke")
    public ResponseEntity<String> RevokeDevice(@PathVariable String id, @RequestHeader("Bearer") String token) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> GetDevice(@RequestHeader("Bearer") String token, @PathVariable String id) {
        return ResponseEntity.ok("Device information for ID: " + id);
    }

    @PostMapping
    public ResponseEntity<CreateDeviceResponse> CreateDevice(@RequestHeader("Bearer") String token,
            @RequestBody CreateDeviceRequest createDeviceRequest) {
        NewDevice newDevice = new NewDevice(createDeviceRequest.description(), createDeviceRequest.location(),
                createDeviceRequest.deviceMetrics());
        CreateDeviceResponse response = createDeviceUseCase.execute(newDevice);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping(value = "/devices/certificate", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> signRequest(@Valid @RequestBody SignCertificateRequest signRequest) {
        try {
            CertificateResponse response = authenticationService.signCertificate(signRequest);
            return ResponseEntity.ok(response);
        } catch (CertificateEncodingException | NameNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/{id}/metrics")
    public ResponseEntity<String> GetDeviceMetrics(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @GetMapping("/metrics")
    public ResponseEntity<String> GetAllMetrics(@RequestHeader("Bearer") String token) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}
