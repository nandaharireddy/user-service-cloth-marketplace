package com.clothmarket.user;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

/**
 * Main entry point for the User Service microservice.
 * Handles authentication, JWT issuance and validation, user registration,
 * and user address management in the clothing marketplace.
 */
@SpringBootApplication
@EnableDiscoveryClient
public class UserServiceApplication {

    /**
     * Main method to bootstrap the User Service Spring Boot application.
     *
     * @param args command line arguments passed during startup
     */
    public static void main(String[] args) {
        SpringApplication.run(UserServiceApplication.class, args);
    }
}
