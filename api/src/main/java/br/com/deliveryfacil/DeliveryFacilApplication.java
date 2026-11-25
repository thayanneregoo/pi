package br.com.deliveryfacil;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DeliveryFacilApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeliveryFacilApplication.class, args);
    }
}
