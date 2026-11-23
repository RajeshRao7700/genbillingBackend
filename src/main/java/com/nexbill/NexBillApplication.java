package com.nexbill;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class NexBillApplication {

    public static void main(String[] args) {
        SpringApplication.run(NexBillApplication.class, args);
    }
}
