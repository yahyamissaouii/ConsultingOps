package com.consultingops.timesheetservice.service;

import com.consultingops.timesheetservice.exception.UnauthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class InternalAuthService {

    private final String expectedApiKey;

    public InternalAuthService(@Value("${app.security.internal-api-key}") String expectedApiKey) {
        this.expectedApiKey = expectedApiKey;
    }

    public void assertValid(String apiKey) {
        if (apiKey == null || !apiKey.equals(expectedApiKey)) {
            throw new UnauthorizedException("Invalid internal API key");
        }
    }
}
