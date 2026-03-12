package com.example.demo.endpoint;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import com.example.demo.gen.LoginUserRequest;
import com.example.demo.gen.LoginUserResponse;
import com.example.demo.gen.RegisterUserRequest;
import com.example.demo.gen.RegisterUserResponse;
import com.example.demo.gen.ValidateTokenRequest;
import com.example.demo.gen.ValidateTokenResponse;
import com.example.demo.service.AuthService;

// SOAP endpoint - Batalgaajuulaltiin uilchilgee
@Endpoint
public class AuthEndpoint {

    private static final Logger log = LoggerFactory.getLogger(AuthEndpoint.class);
    private static final String NAMESPACE_URI = "http://example.com/auth";

    private final AuthService authService;

    public AuthEndpoint(AuthService authService) {
        this.authService = authService;
    }

    // Hereglegch burtgeh
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "registerUserRequest")
    @ResponsePayload
    public RegisterUserResponse registerUser(@RequestPayload RegisterUserRequest request) {
        log.info("===== [SOAP] RegisterUser huselt irlee | username: {} =====", request.getUsername());
        RegisterUserResponse response = new RegisterUserResponse();
        try {
            String message = authService.registerUser(request.getUsername(), request.getPassword());
            response.setSuccess(true);
            response.setMessage(message);
            log.info("[SOAP] Hereglegch amjilttai burtgegdlee: {}", request.getUsername());
        } catch (Exception e) {
            response.setSuccess(false);
            response.setMessage(e.getMessage());
            log.warn("[SOAP] Burtgel amjiltgui: {}", e.getMessage());
        }
        return response;
    }

    // Hereglegch nevtreh
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "loginUserRequest")
    @ResponsePayload
    public LoginUserResponse loginUser(@RequestPayload LoginUserRequest request) {
        log.info("===== [SOAP] LoginUser huselt irlee | username: {} =====", request.getUsername());
        LoginUserResponse response = new LoginUserResponse();
        try {
            String token = authService.loginUser(request.getUsername(), request.getPassword());
            response.setSuccess(true);
            response.setToken(token);
            response.setMessage("Amjilttai nevterlee (Login successful)");
            log.info("[SOAP] JWT token uusgelee, hereglegch: {}", request.getUsername());
            log.info("[SOAP] Token: {}...{}", token.substring(0, 20), token.substring(token.length() - 10));
        } catch (Exception e) {
            response.setSuccess(false);
            response.setToken("");
            response.setMessage(e.getMessage());
            log.warn("[SOAP] Nevtrelt amjiltgui: {}", e.getMessage());
        }
        return response;
    }

    // Token shalgah
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "validateTokenRequest")
    @ResponsePayload
    public ValidateTokenResponse validateToken(@RequestPayload ValidateTokenRequest request) {
        log.info("===== [SOAP] ValidateToken huselt irlee =====");
        ValidateTokenResponse response = new ValidateTokenResponse();
        boolean valid = authService.validateToken(request.getToken());
        response.setValid(valid);
        if (valid) {
            String username = authService.getUsernameFromToken(request.getToken());
            response.setUsername(username);
            log.info("[SOAP] Token BATALGAAJLAA (VALID) | hereglegch: {}", username);
        } else {
            response.setUsername("");
            log.warn("[SOAP] Token HUCHIINGUI (INVALID)");
        }
        return response;
    }
}
