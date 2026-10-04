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

import com.server.sensor_log.application.services.AuthenticationService;
import com.server.sensor_log.application.services.VaultService;
import com.server.sensor_log.infra.repository.DeviceRepository;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthenticationService authenticationService;

    public AuthController(VaultService vaultService, DeviceRepository deviceRepository,
            AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }


}