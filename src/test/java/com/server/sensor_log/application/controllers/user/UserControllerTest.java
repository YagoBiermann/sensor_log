package com.server.sensor_log.application.controllers.user;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import com.server.sensor_log.application.controllers.user.dto.LoginRequest;
import com.server.sensor_log.application.controllers.user.dto.LoginResponse;
import com.server.sensor_log.application.controllers.user.dto.RegisterRequest;
import com.server.sensor_log.application.exceptions.InvalidCredentialsException;
import com.server.sensor_log.application.exceptions.UserAlreadyExistsException;
import com.server.sensor_log.application.services.AuthenticationService;

class UserControllerTest {
    @Test
    void registersAndLogsIn() throws Exception {
        AuthenticationService service = mock(AuthenticationService.class);
        UserController controller = new UserController(service);

        assertEquals(HttpStatus.CREATED, controller.register(
                new RegisterRequest("user@example.com", "password", null)).getStatusCode());
        when(service.login(any())).thenReturn(new LoginResponse("jwt"));
        assertEquals(HttpStatus.OK, controller.login(new LoginRequest("user@example.com", "password")).getStatusCode());
    }

    @Test
    void mapsExpectedAuthenticationErrors() throws Exception {
        AuthenticationService service = mock(AuthenticationService.class);
        UserController controller = new UserController(service);
        doThrow(new UserAlreadyExistsException("exists")).when(service).register(any());
        when(service.login(any())).thenThrow(new InvalidCredentialsException("invalid"));

        assertEquals(HttpStatus.CONFLICT, controller.register(
                new RegisterRequest("user@example.com", "password", null)).getStatusCode());
        assertEquals(HttpStatus.UNAUTHORIZED, controller.login(
                new LoginRequest("user@example.com", "password")).getStatusCode());
    }
}
