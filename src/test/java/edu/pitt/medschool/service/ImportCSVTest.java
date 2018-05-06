package edu.pitt.medschool.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = {"edu.pitt.medschool"})
public class ImportCSVTest implements CommandLineRunner {

    @Autowired
    ImportCsvService ics;
    @Value("${machine}")
    private String uuid;

    public static void main(String[] args) {
        SpringApplication.run(ImportCSVTest.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        ics._test();
    }

}