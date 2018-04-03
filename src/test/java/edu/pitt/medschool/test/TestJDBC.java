package edu.pitt.medschool.test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import edu.pitt.medschool.service.ImportCsvService;

/**
 * Test JDBC related works
 */
@SpringBootApplication(scanBasePackages = {"edu.pitt.medschool"})
public class TestJDBC implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(TestJDBC.class, args);
    }

    @Autowired
    ImportCsvService ic;

    @Override
    public void run(String... args) throws Exception {
        ic._test();
        // Exit routine
        // SpringApplication.exit(context);
    }

}
