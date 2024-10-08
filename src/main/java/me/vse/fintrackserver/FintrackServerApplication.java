package me.vse.fintrackserver;

import jakarta.persistence.EntityManager;
import me.vse.fintrackserver.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

import java.util.UUID;

@SpringBootApplication
@EnableJpaRepositories(basePackages = "me.vse.fintrackserver")
public class FintrackServerApplication {


    public static void main(String[] args) {
        SpringApplication.run(FintrackServerApplication.class, args);
    }

}
