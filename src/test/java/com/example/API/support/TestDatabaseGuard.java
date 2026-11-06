package com.example.API.support;

import org.springframework.context.ApplicationContextInitializer;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.Objects;

public class TestDatabaseGuard implements ApplicationContextInitializer<ConfigurableApplicationContext> {
    @Override
    public void initialize(ConfigurableApplicationContext context) {
        var environment = context.getEnvironment();

        if(!environment.matchesProfiles("test")){
            throw new IllegalStateException("Refusing test startup: profile does not match the active environment.");
        }

        if (!"jdbc:mysql://localhost:3306/gymsecuritytest"
                .equals(environment.getProperty("spring.datasource.url"))) {
            throw new IllegalStateException("Refusing test startup: datasource URL must target gymsecuritytest.");
        }

        if(!Objects.equals(environment.getProperty("spring.datasource.username"), "gym_test")){
            throw new IllegalStateException("Refusing test startup: username does not match correct datasource username.");
        }
    }
}
