package com.nttdata.bootcamp.msmovement;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;

/**
 * Class MsMovementApplication Main.
 * Movement microservice class MsMovementApplication.
 */
@SpringBootApplication
@EnableEurekaClient
public class MsMovementApplication {

    public static void main(String[] args) {
        SpringApplication.run(MsMovementApplication.class, args);
    }

}