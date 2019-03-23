package com.github.svserge89.partpostconverter;

import com.github.svserge89.partpostconverter.corrector.FileCorrector;
import com.github.svserge89.partpostconverter.exception.*;
import com.github.svserge89.partpostconverter.io.DirectoryWalker;
import com.github.svserge89.partpostconverter.resolver.ArgumentResolver;
import com.github.svserge89.partpostconverter.resolver.PropertyResolver;
import com.github.svserge89.partpostconverter.resolver.RegionResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Application {
    private static final Logger log = LoggerFactory.getLogger(Application.class);

    private static final String INPUT_DIR_PARAM = "input";
    private static final String OUTPUT_DIR_PARAM = "output";
    private static final String POST_OFFICE_NUM_PARAM = "office-number";
    private static final String POST_INDEX_FILE_PARAM = "post-index-db";
    private static final String CONFIG_FILE_PARAM = "config-file";

    public static void main(String[] args) throws IOException {
        log.info("Start application");
        try {
            ArgumentResolver argumentResolver = new ArgumentResolver(args);

            PropertyResolver propertyResolver = getPropertyResolver(argumentResolver);

            Path inputDirectory = getInputDirectory(argumentResolver, propertyResolver);
            Path outputDirectory = getOutputDirectory(argumentResolver, propertyResolver);
            Path dbfFile = getDBFFile(argumentResolver, propertyResolver);

            int defaultPostOfficeNumber = getPostOfficeNumber(argumentResolver, propertyResolver);

            DirectoryWalker directoryWalker = new DirectoryWalker(inputDirectory, outputDirectory);

            RegionResolver regionResolver = new RegionResolver(dbfFile);

            directoryWalker.runCorrector(regionResolver, defaultPostOfficeNumber);
        } catch (FileCorrectorException | RegionResolverException | DirectoryWalkerException e) {
            showError(e);
            System.exit(1);
        } catch (ArgumentResolverException | PropertyResolverException |
                IncorrectParamException e) {
            showError(e);
            showUsage();
            System.exit(1);
        }
        log.info("Application closed without errors");
    }

    private static void showUsage() {
        System.out.printf("Usage: java partpostconverter [-%s input_directory] " +
                        "[-%s output_directory] [-%s post_index_file] " +
                        "[-%s default_index] [-%s config_file]%n",
                INPUT_DIR_PARAM, OUTPUT_DIR_PARAM, POST_INDEX_FILE_PARAM,
                POST_OFFICE_NUM_PARAM, CONFIG_FILE_PARAM);
    }

    private static void showError(Throwable throwable) {
        log.error("Application closed with exception", throwable);

        System.err.println("Some errors occurred. Details in the log.");
    }

    private static int getPostOfficeNumber(ArgumentResolver argumentResolver,
                                           PropertyResolver propertyResolver) {
        String value;

        if (argumentResolver.containsArgument(POST_OFFICE_NUM_PARAM)) {
            value = argumentResolver.getValue(POST_OFFICE_NUM_PARAM);
        } else if (propertyResolver.containsProperty(POST_OFFICE_NUM_PARAM)) {
            value = propertyResolver.getProperty(POST_OFFICE_NUM_PARAM);
        } else {
            log.error("Can't find parameter \"{}\"", POST_OFFICE_NUM_PARAM);

            throw new IncorrectParamException("Can't find parameter \"" + POST_OFFICE_NUM_PARAM +
                    "\"");
        }

        if (!value.matches(FileCorrector.POST_OFFICE_NUMBER_PATTERN)) {
            log.error("Incorrect parameter \"{}\"", value);

            throw new IncorrectParamException("Incorrect parameter \"" + value + "\"");
        }

        return Integer.parseInt(value);
    }

    private static Path getInputDirectory(ArgumentResolver argumentResolver,
                                          PropertyResolver propertyResolver) {
        return getFilePath(argumentResolver, propertyResolver, INPUT_DIR_PARAM,
                INPUT_DIR_PARAM);
    }

    private static Path getOutputDirectory(ArgumentResolver argumentResolver,
                                           PropertyResolver propertyResolver) {
        return getFilePath(argumentResolver, propertyResolver, OUTPUT_DIR_PARAM,
                OUTPUT_DIR_PARAM);
    }

    private static Path getDBFFile(ArgumentResolver argumentResolver,
                                    PropertyResolver propertyResolver) {
        return getFilePath(argumentResolver, propertyResolver, POST_INDEX_FILE_PARAM,
                POST_INDEX_FILE_PARAM);
    }

    private static Path getFilePath(ArgumentResolver argumentResolver,
                                    PropertyResolver propertyResolver,
                                    String argumentName, String propertyName) {
        if (argumentResolver.containsArgument(argumentName)) {
            return Paths.get(argumentResolver.getValue(argumentName));
        } else if (propertyResolver.containsProperty(propertyName)) {
            return Paths.get(propertyResolver.getProperty(propertyName));
        } else {
            log.error("Can't find parameter \"{}\"", propertyName);

            throw new IncorrectParamException("Can't find parameter \"" + propertyName + "\"");
        }
    }

    private static PropertyResolver getPropertyResolver(ArgumentResolver argumentResolver) {
        if (argumentResolver.containsArgument(CONFIG_FILE_PARAM)) {
            return new PropertyResolver(Paths.get(argumentResolver.getValue(CONFIG_FILE_PARAM)));
        } else {
            return new PropertyResolver();
        }
    }
}
