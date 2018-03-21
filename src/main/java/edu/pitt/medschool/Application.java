package edu.pitt.medschool;

import java.io.File;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import edu.pitt.medschool.config.InfluxappConfig;

@SpringBootApplication
public class Application {

    /**
     * Some init work for this system
     */
    private static void init_system() {
        // Create mandatory dirs
        File sysTmpDir = InfluxappConfig.TMP_DIR.getDir("imp_progress");
        if (!sysTmpDir.exists())
            if (!sysTmpDir.mkdirs())
                throw new RuntimeException("Temp dir creation fail!");
    }

    public static void main(String[] args) {
        init_system();
        // Start Spring Application
        SpringApplication.run(Application.class, args);
    }
}
