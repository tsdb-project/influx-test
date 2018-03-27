package edu.pitt.medschool.framework.util;

import edu.pitt.medschool.service.ImportMetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;

/**
 * Test JDBC related works
 */
@SpringBootApplication(scanBasePackages = { "edu.pitt.medschool"})
public class TestJDBC implements CommandLineRunner {

    @Autowired
    private ConfigurableApplicationContext context;

    public static void main(String[] args) {
        SpringApplication.run(TestJDBC.class, args);
    }

    @Autowired
    ImportMetadataService importMetadataService;

    @Override
    public void run(String... args) {
        importMetadataService.DoImport("E:\\UPMC\\TSDB\\PCASDatabase_DATA_2018-02-21_0905.csv");
        // Exit routine
        SpringApplication.exit(context);
    }

}
