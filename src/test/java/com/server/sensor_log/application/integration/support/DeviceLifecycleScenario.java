package com.server.sensor_log.application.integration.support;

import java.util.List;

import com.server.sensor_log.application.controllers.device.dto.ClaimDeviceRequest;
import com.server.sensor_log.application.controllers.device.dto.ClaimDeviceResponse;
import com.server.sensor_log.application.controllers.device.dto.CreateDeviceResponse;
import com.server.sensor_log.application.controllers.device.dto.FirstBootRequest;
import com.server.sensor_log.application.controllers.device.dto.FirstBootResponse;
import com.server.sensor_log.application.controllers.device.dto.NewDevice;
import com.server.sensor_log.application.controllers.user.dto.RegisterRequest;
import com.server.sensor_log.application.services.AuthenticationService;
import com.server.sensor_log.application.services.JwtService;
import com.server.sensor_log.application.usecases.BootDeviceUseCase;
import com.server.sensor_log.application.usecases.ClaimDeviceUseCase;
import com.server.sensor_log.application.usecases.CreateDeviceUseCase;
import com.server.sensor_log.domain.model.device.MetricsType;
import com.server.sensor_log.domain.model.user.User;
import com.server.sensor_log.infra.repository.DeviceRepository;
import com.server.sensor_log.infra.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

public final class DeviceLifecycleScenario {

    private static final String EMAIL = "owner@example.com";
    private static final String PASSWORD = "correct-password";
    private static final String CSR =
            "-----BEGIN CERTIFICATE REQUEST-----\nYWJj\n-----END CERTIFICATE REQUEST-----";

    private final DeviceRepository deviceRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final TestIdentityGenerator identityGenerator = new TestIdentityGenerator();
    private final AuthenticationService authenticationService;
    private final CreateDeviceUseCase createDeviceUseCase;
    private final BootDeviceUseCase bootDeviceUseCase;
    private final ClaimDeviceUseCase claimDeviceUseCase;

    public DeviceLifecycleScenario(
            DeviceRepository deviceRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder) {
        this.deviceRepository = deviceRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationService = new TestAuthenticationService(
                userRepository, passwordEncoder, identityGenerator);
        this.createDeviceUseCase = new CreateDeviceUseCase(
                deviceRepository, identityGenerator, passwordEncoder,
                new JwtService("integration-test-secret-0123456789-abcdef"));
        this.bootDeviceUseCase = new BootDeviceUseCase(
                deviceRepository, identityGenerator, passwordEncoder);
        this.claimDeviceUseCase = new ClaimDeviceUseCase(
                deviceRepository, passwordEncoder, authenticationService);
    }

    public User registerUser() {
        authenticationService.register(new RegisterRequest(EMAIL, PASSWORD, "Owner"));
        return userRepository.findByEmail(EMAIL);
    }

    public CreateDeviceResponse createDevice() {
        return createDeviceUseCase.execute(new NewDevice(
                List.of(MetricsType.TEMPERATURE, MetricsType.HUMIDITY)));
    }

    public FirstBootResponse bootDevice(CreateDeviceResponse device) {
        return bootDeviceUseCase.execute(
                new FirstBootRequest(device.serialNumber(), device.bootstrapToken()));
    }

    public ClaimDeviceResponse claimDevice(
            CreateDeviceResponse device,
            String redeemCode,
            User user) throws Exception {
        return claimDeviceUseCase.execute(new ClaimDeviceRequest(
                device.serialNumber(), user.getUserId(), redeemCode, CSR));
    }
}
