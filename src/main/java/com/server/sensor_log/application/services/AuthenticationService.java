package com.server.sensor_log.application.services;

import java.security.cert.X509Certificate;
import java.time.Duration;
import java.math.BigInteger;
import java.util.Map;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.vault.core.VaultPkiOperations;
import org.springframework.vault.core.VaultTemplate;
import org.springframework.vault.support.VaultCertificateRequest;
import org.springframework.vault.support.VaultSignCertificateRequestResponse;

import com.server.sensor_log.application.controllers.device.dto.ClaimDeviceRequest;
import com.server.sensor_log.application.controllers.user.dto.LoginRequest;
import com.server.sensor_log.application.controllers.user.dto.LoginResponse;
import com.server.sensor_log.application.controllers.user.dto.RegisterRequest;
import com.server.sensor_log.application.exceptions.InvalidCredentialsException;
import com.server.sensor_log.application.exceptions.UserAlreadyExistsException;
import com.server.sensor_log.domain.model.device.RandomGenerator;
import com.server.sensor_log.domain.model.device.Role;
import com.server.sensor_log.domain.model.user.User;
import com.server.sensor_log.infra.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {
        private final VaultTemplate vaultTemplate;
        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final JwtService jwtService;
        private final RandomGenerator randomGenerator;

        public LoginResponse login(LoginRequest loginRequest) throws InvalidCredentialsException {
                User user = userRepository.findByEmail(loginRequest.email());

                if (user == null || !passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
                        throw new InvalidCredentialsException("Invalid credentials.");
                }

                return new LoginResponse(jwtService.generateToken(user.getUserId(), user.getRole().name()));
        }

        public void register(RegisterRequest registerRequest) throws UserAlreadyExistsException {
                Boolean userExists = userRepository.existsByEmail(registerRequest.email());
                if (userExists) {
                        throw new UserAlreadyExistsException("this user already exists");
                }
                String encodedPassword = passwordEncoder.encode(registerRequest.password());
                User user = new User(randomGenerator, registerRequest.email(), encodedPassword);
                userRepository.save(user);
        }

        public X509Certificate signCertificate(ClaimDeviceRequest signRequest) {
                VaultPkiOperations pkiOperations = vaultTemplate.opsForPki("pki_int");
                VaultCertificateRequest certificateSignRequest = VaultCertificateRequest.builder()
                                .commonName(signRequest.serialNumber())
                                .ttl(Duration.ofHours(24))
                                .userId(signRequest.serialNumber())
                                .build();
                VaultSignCertificateRequestResponse response = pkiOperations.signCertificateRequest(Role.DEVICE.name(),
                                signRequest.csr(), certificateSignRequest);
                X509Certificate certificate = response.getRequiredData().getX509Certificate();

                return certificate;
        }

        public void revokeCertificate(String certificateSerialNumber) {
                if (certificateSerialNumber == null || certificateSerialNumber.isBlank()) {
                        throw new IllegalArgumentException("Certificate serial number is required");
                }

                vaultTemplate.write("pki_int/revoke", Map.of(
                                "serial_number", toVaultSerialNumber(certificateSerialNumber)));
        }

        private String toVaultSerialNumber(String certificateSerialNumber) {
                if (!certificateSerialNumber.matches("\\d+")) {
                        return certificateSerialNumber;
                }

                String hexadecimal = new BigInteger(certificateSerialNumber).toString(16);
                if ((hexadecimal.length() & 1) != 0) {
                        hexadecimal = "0" + hexadecimal;
                }

                StringBuilder formatted = new StringBuilder();
                for (int index = 0; index < hexadecimal.length(); index += 2) {
                        if (formatted.length() > 0) {
                                formatted.append(':');
                        }
                        formatted.append(hexadecimal, index, index + 2);
                }
                return formatted.toString();
        }
}