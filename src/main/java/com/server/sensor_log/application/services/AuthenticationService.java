package com.server.sensor_log.application.services;

import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.util.Base64;

import javax.naming.NameNotFoundException;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.server.sensor_log.application.controllers.dto.CertificateResponse;
import com.server.sensor_log.application.controllers.dto.LoginRequest;
import com.server.sensor_log.application.controllers.dto.LoginResponse;
import com.server.sensor_log.application.controllers.dto.RegisterRequest;
import com.server.sensor_log.application.controllers.dto.SignCertificateRequest;
import com.server.sensor_log.application.exceptions.InvalidCredentialsException;
import com.server.sensor_log.application.exceptions.UserAlreadyExistsException;
import com.server.sensor_log.domain.model.device.Device;
import com.server.sensor_log.domain.model.device.RandomGenerator;
import com.server.sensor_log.domain.model.user.User;
import com.server.sensor_log.infra.repository.DeviceRepository;
import com.server.sensor_log.infra.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

        private final UserRepository userRepository;
        private final DeviceRepository deviceRepository;
        private final PasswordEncoder passwordEncoder;
        private final VaultService vaultService;
        private final JwtService jwtService;
        private final RandomGenerator randomGenerator;

        public LoginResponse login(LoginRequest loginRequest) throws InvalidCredentialsException {
                User user = userRepository.findByEmail(loginRequest.email());

                if (!passwordEncoder.matches(loginRequest.password(), user.getPassword())) {
                        throw new InvalidCredentialsException("Invalid credentials.");
                }

                return new LoginResponse(jwtService.generateToken(user));
        }

        public void register(RegisterRequest registerRequest) throws UserAlreadyExistsException {
                Boolean userExists = userRepository.ExistsByEmail(registerRequest.email());
                if (userExists) {
                        throw new UserAlreadyExistsException("this user already exists");
                }
                String encodedPassword = passwordEncoder.encode(registerRequest.password());
                User user = new User(randomGenerator, registerRequest.email(), encodedPassword);
                userRepository.save(user);
        }

        public CertificateResponse signCertificate(SignCertificateRequest request)
                        throws SecurityException, NameNotFoundException, CertificateEncodingException {

                Device device = deviceRepository.findById(request.serialNumber())
                                .orElseThrow(() -> new NameNotFoundException("Device with serial number "
                                                + request.serialNumber() + " does not exist."));
                device.claim(request.ownerId(), request.bootstrapToken(), request.redeemToken());
                X509Certificate signedCertificate = vaultService.signCertificate(request);

                deviceRepository.save(device);

                return new CertificateResponse(
                                toPem(signedCertificate),
                                signedCertificate.getSerialNumber().toString(),
                                signedCertificate.getNotAfter()
                                                .toInstant()
                                                .toString());
        }

        private String toPem(X509Certificate certificate) throws CertificateEncodingException {
                Base64.Encoder encoder = Base64.getMimeEncoder(64, "\n".getBytes());
                String encoded = encoder.encodeToString(certificate.getEncoded());
                return "-----BEGIN CERTIFICATE-----\n" + encoded + "\n-----END CERTIFICATE-----";
        }
}