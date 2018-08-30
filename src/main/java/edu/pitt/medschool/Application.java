package edu.pitt.medschool;

import edu.pitt.medschool.framework.util.TimeUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Arrays;
import java.util.Date;
import java.util.TimeZone;

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
    }
}
