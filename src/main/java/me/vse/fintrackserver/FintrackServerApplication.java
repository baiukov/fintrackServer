package me.vse.fintrackserver;

import jakarta.persistence.EntityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class FintrackServerApplication {

    @Autowired
    private static EntityManager entityManager;

    public static void main(String[] args) {

//        Test test = new Test(1L, "icon", "name", "color", null, null);
//        entityManager.persist(test);
        SpringApplication.run(FintrackServerApplication.class, args);
    }

}
