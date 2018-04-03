package edu.pitt.medschool.test;

import edu.pitt.medschool.model.dao.ImportedFileDao;
import edu.pitt.medschool.model.dao.PatientDao;
import edu.pitt.medschool.model.dto.Patient;
import edu.pitt.medschool.model.dto.PatientExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import edu.pitt.medschool.service.ImportCsvService;

import java.util.ArrayList;
import java.util.List;

/**
 * Test JDBC related works
 */
@SpringBootApplication(scanBasePackages = {"edu.pitt.medschool"})
public class TestJDBC implements CommandLineRunner {

    public static void main(String[] args) {
        SpringApplication.run(TestJDBC.class, args);
    }

    @Value("${machine}")
    private String uuid;

    @Autowired
    PatientDao pd;

    @Autowired
    ImportedFileDao ifd;

    @Override
    public void run(String... args) throws Exception {

    }

}
