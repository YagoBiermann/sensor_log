package com.server.sensor_log.domain.model.device;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;

import org.springframework.data.annotation.Transient;

import com.server.sensor_log.application.exceptions.ExpiredCodeException;
import com.server.sensor_log.application.exceptions.MaxAttemptsFailedException;
import com.server.sensor_log.application.exceptions.RetryBeforeCooldownException;

public class RedeemCode {
    public static final Duration LIFETIME = Duration.ofMinutes(10);
    public static final Duration COOLDOWN = Duration.ofSeconds(30);
    public static final int MAX_FAILED_ATTEMPTS = 5;
    @Transient
    private final RandomGenerator codeGenerator;
    private String code;
    private Instant expiresAt;
    private int failedAttempts;
    private Instant cooldownUntil;

    public RedeemCode(String code, Instant expiresAt, int failedAttempts, Instant cooldownUntil, RandomGenerator codeGenerator) {
        this.code = code;
        this.expiresAt = expiresAt;
        this.failedAttempts = failedAttempts;
        this.cooldownUntil = cooldownUntil;
        this.codeGenerator = codeGenerator;
    }

    public static RedeemCode issue(RandomGenerator codeGenerator) {
        String code = codeGenerator.generateRedeemCode();
        return new RedeemCode(code, Instant.now().plus(LIFETIME), 0, Instant.EPOCH, codeGenerator);
    }

    public static RedeemCode restore(String encodedValue, Instant expiresAt, int failedAttempts, Instant cooldownUntil, RandomGenerator codeGenerator) {
        return new RedeemCode(encodedValue, expiresAt, failedAttempts, cooldownUntil, codeGenerator);
    }

    public boolean isAllowedToRetry() {
        return !this.isInCooldown() && !this.isExpired();
    }
    public boolean isInCooldown(){
        return !cooldownUntil.equals(Instant.EPOCH) && Instant.now().isBefore(cooldownUntil);
    }
    public boolean cooldownHasFinished() {
        return !cooldownUntil.equals(Instant.EPOCH) && Instant.now().isAfter(cooldownUntil);
    }
    public boolean maxFailedAttemptsReached() {
        return failedAttempts >= MAX_FAILED_ATTEMPTS;
    }

    public boolean isExpired() {
        return Instant.now().isAfter(expiresAt);
    }

    public boolean matches(String code) throws RetryBeforeCooldownException, MaxAttemptsFailedException, ExpiredCodeException {
        if(this.isExpired()){
            throw new ExpiredCodeException("The given code has expired.");
        }
        if(maxFailedAttemptsReached()) {
            if(isInCooldown()){
                throw new RetryBeforeCooldownException("Please, wait " + COOLDOWN.getSeconds() + " seconds before trying again.");
            }
            if(cooldownHasFinished()){
                this.resetFailedAttempts();
            }
        }
        boolean matches = this.code.equals(code);
        if(!matches){
            this.failedAttempts++;
            if(maxFailedAttemptsReached()) {
                this.cooldownUntil = Instant.now().plus(COOLDOWN);
                throw new MaxAttemptsFailedException("Exceeded maximum failed attempts: " + this.failedAttempts);
            }
        } else {
            this.failedAttempts = 0;
        }

        return matches;
    }

    public void resetFailedAttempts() {
        this.failedAttempts = 0;
        this.cooldownUntil = Instant.EPOCH;
    }

    public RedeemCode withFailedAttempt() throws RetryBeforeCooldownException, MaxAttemptsFailedException, ExpiredCodeException {
        return new RedeemCode(this.code, this.expiresAt, this.failedAttempts, this.cooldownUntil, this.codeGenerator);
    }

    public String toString() {
        return this.code;
    }

    public boolean Equals(RedeemCode other) {
        boolean matches = code != null && MessageDigest.isEqual(
        this.code.getBytes(StandardCharsets.UTF_8),
        other.code.getBytes(StandardCharsets.UTF_8));

        return matches;
    }
}
