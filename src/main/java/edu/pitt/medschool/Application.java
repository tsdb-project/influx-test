package edu.pitt.medschool;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

import edu.pitt.medschool.config.InfluxappConfig;
import edu.pitt.medschool.framework.util.Util;
import edu.pitt.medschool.service.AggregationService;
import edu.pitt.medschool.service.AutoImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import edu.pitt.medschool.framework.util.TimeUtil;

@SpringBootApplication
public class Application implements ApplicationRunner {
    @Autowired
    AutoImportService autoImportService;
    @Autowired
    AggregationService aggregationService;


    private static final Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String... args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(ApplicationArguments args) throws IOException {
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

        // For storing zip files
        File outputDir = new File(InfluxappConfig.ARCHIVE_DIRECTORY);

        if (!outputDir.exists()) {
            try {
                if (!outputDir.mkdirs()) {
                    throw new RuntimeException("Failed to create output folder.");
                }
            } catch (SecurityException se) {
                logger.error(Util.stackTraceErrorToString(se));
            }
        }
//        autoImportService.initImport();
    }
}
