package com.server.sensor_log.application.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.server.sensor_log.application.controllers.dto.NewDevice;
import com.server.sensor_log.application.services.JwtService;

@RestController
@RequestMapping("/api/device")
public class DeviceController {
    private final JwtService jwtService;

    public DeviceController(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @GetMapping("/{id}")
    public ResponseEntity<String> GetDevice(@RequestHeader("Bearer") String token, @PathVariable String id) {
        return ResponseEntity.ok("Device information for ID: " + id);
    }

    @PostMapping
    public ResponseEntity<String> CreateDevice(@RequestHeader("Bearer") String token,
            @RequestBody NewDevice newDevice) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/{id}/metrics")
    public ResponseEntity<String> GetDeviceMetrics(@PathVariable String id) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
    }

    @PostMapping("/metrics")
    public ResponseEntity<String> GetAllMetrics(@RequestHeader("Bearer") String token) {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).build();
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
