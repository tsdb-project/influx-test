package edu.pitt.medschool.framework.util;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Test JDBC related works
 */
@SpringBootApplication
public class TestJDBC implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(TestJDBC.class, args);
    }

    @Override
    public void run(String... args) {

    }

}
