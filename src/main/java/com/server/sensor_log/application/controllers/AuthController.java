package com.server.sensor_log.application.controllers;

import java.net.URI;
import java.security.cert.CertificateEncodingException;

import javax.naming.NameNotFoundException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.server.sensor_log.application.controllers.dto.CertificateResponse;
import com.server.sensor_log.application.controllers.dto.LoginRequest;
import com.server.sensor_log.application.controllers.dto.LoginResponse;
import com.server.sensor_log.application.controllers.dto.RegisterRequest;
import com.server.sensor_log.application.controllers.dto.SignCertificateRequest;
import com.server.sensor_log.application.exceptions.InvalidCredentialsException;
import com.server.sensor_log.application.exceptions.UserAlreadyExistsException;
import com.server.sensor_log.application.services.AuthenticationService;
import com.server.sensor_log.application.services.VaultService;
import com.server.sensor_log.infra.repository.DeviceRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/devices/auth")
public class AuthController {
    private final AuthenticationService authenticationService;

    public AuthController(VaultService vaultService, DeviceRepository deviceRepository,
            AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @PostMapping(value = "/certificate/sign", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> signRequest(@Valid @RequestBody SignCertificateRequest signRequest) {
        try {
            CertificateResponse response = authenticationService.signCertificate(signRequest);
            return ResponseEntity.ok(response);
        } catch (CertificateEncodingException | NameNotFoundException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping(value = "/register", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest registerRequest) {
        try {
            authenticationService.register(registerRequest);

            return ResponseEntity.created(URI.create("/api/auth/login")).build();
        } catch (UserAlreadyExistsException e) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("Something is not working as expected. \nPlease try again later.");
        }
    }

    @PostMapping(value = "/login", consumes = "application/json", produces = "application/json")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest loginRequest) {
        try {
            LoginResponse response = authenticationService.login(loginRequest);

            return ResponseEntity.ok(response);
        } catch (InvalidCredentialsException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(e);
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body("Something is not working as expected. \nPlease try again later.");
        }
    }

    @PostMapping("/{id}/claim")
    public ResponseEntity<String> ClaimDevice(@PathVariable String id, @RequestBody String claimCode,
            @RequestHeader("Bearer") String token) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/{id}/revoke")
    public ResponseEntity<String> RevokeDevice(@PathVariable String id, @RequestHeader("Bearer") String token) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }
}