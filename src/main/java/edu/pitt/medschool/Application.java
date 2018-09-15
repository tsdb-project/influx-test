package edu.pitt.medschool;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import edu.pitt.medschool.framework.util.TimeUtil;

@SpringBootApplication
public class Application implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String... args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        TimeZone.setDefault(TimeUtil.nycTimeZone);
        logger.warn("BrainFlux start time: " + new Date());

        logger.info("Application started with command-line arguments: {}", Arrays.toString(args.getSourceArgs()));
        logger.info("NonOptionArgs: {}", args.getNonOptionArgs());
        logger.info("OptionNames: {}", args.getOptionNames());

        for (String name : args.getOptionNames()) {
            logger.info("arg->" + name + "=" + args.getOptionValues(name));
        }

        if (!args.containsOption("machine")) {
            logger.error("Using default machine name: Anyone!!!");
        }

        String path = "output";
        File outputDir = new File(path);

        boolean dirCreationSuccess = true;

        if (!outputDir.exists()) {
            String err = "Failed to create 'Results' dir. ";
            try {
                if (!outputDir.mkdirs()) {
                    dirCreationSuccess = false;
                }
            } catch (SecurityException se) {
                err += se.getLocalizedMessage();
                dirCreationSuccess = false;
            }
            // Use a flag for flexible work flow
            if (!dirCreationSuccess) {
                logger.error(err);
            }
        }

    }
}
