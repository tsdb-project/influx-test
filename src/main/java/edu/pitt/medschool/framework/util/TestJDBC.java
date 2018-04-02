package edu.pitt.medschool.framework.util;

import edu.pitt.medschool.service.ImportCsvService;
import edu.pitt.medschool.service.ImportMetadataService;
import edu.pitt.medschool.service.ImportProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

import java.util.UUID;

/**
 * Test JDBC related works
 */
@SpringBootApplication(scanBasePackages = {"edu.pitt.medschool"})
public class TestJDBC implements CommandLineRunner {

    @Autowired
    private ConfigurableApplicationContext context;

    public static void main(String[] args) {
        SpringApplication.run(TestJDBC.class, args);
    }

    @Autowired
    ImportCsvService ic;

    @Override
    public void run(String... args) throws Exception {
        ic.SetLoadFactor(0.001);
        ic._test();
        // Exit routine
        // SpringApplication.exit(context);
    }

}
