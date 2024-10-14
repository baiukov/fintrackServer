package me.vse.fintrackserver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "me.vse.fintrackserver")
public class FintrackServerApplication {


    public static void main(String[] args) {
        SpringApplication.run(FintrackServerApplication.class, args);
    }

}
