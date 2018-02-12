package app;

import app.common.InfluxappConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.File;

@SpringBootApplication
public class InfluxApplication {

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
        SpringApplication.run(InfluxApplication.class, args);
    }
}
