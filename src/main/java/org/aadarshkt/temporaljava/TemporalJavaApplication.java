package org.aadarshkt.temporaljava;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TemporalJavaApplication {
    public static void main(String[] args) {
        SpringApplication.run(TemporalJavaApplication.class, args);
    }
}
