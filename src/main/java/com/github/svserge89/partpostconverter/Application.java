package com.github.svserge89.partpostconverter;

import com.github.svserge89.partpostconverter.exception.ArgumentResolverException;
import com.github.svserge89.partpostconverter.exception.DirectoryWalkerException;
import com.github.svserge89.partpostconverter.exception.FileCorrectorException;
import com.github.svserge89.partpostconverter.exception.RegionResolverException;
import com.github.svserge89.partpostconverter.io.DirectoryWalker;
import com.github.svserge89.partpostconverter.resolver.ArgumentResolver;
import com.github.svserge89.partpostconverter.resolver.RegionResolver;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Application {
    private static final String INPUT_DIR_PARAM = "input";
    private static final String OUTPUT_DIR_PARAM = "output";
    private static final String POST_OFFICE_NUM_PARAM = "office-number";
    private static final String POST_INDEX_FILE_PARAM = "post-index-db";

    public static void main(String[] args) throws IOException {
        try {
            ArgumentResolver argumentResolver = new ArgumentResolver(args);

            if (!checkMainArguments(argumentResolver)) {
                throw new ArgumentResolverException("Important commandline arguments not found");
            }

            Path inputDirectory = Paths.get(argumentResolver.getValue(INPUT_DIR_PARAM));
            Path outputDirectory = Paths.get(argumentResolver.getValue(OUTPUT_DIR_PARAM));
            Path dbfFile = Paths.get(argumentResolver.getValue(POST_INDEX_FILE_PARAM));

            int defaultPostOfficeNumber =
                    Integer.parseInt(argumentResolver.getValue(POST_OFFICE_NUM_PARAM));

            DirectoryWalker directoryWalker = new DirectoryWalker(inputDirectory, outputDirectory);

            RegionResolver regionResolver = new RegionResolver(dbfFile);

            directoryWalker.runCorrector(regionResolver, defaultPostOfficeNumber);
        } catch (FileCorrectorException | RegionResolverException | DirectoryWalkerException e) {
            showError(e);
            System.exit(1);
        } catch (ArgumentResolverException e) {
            showError(e);
            showUsage();
            System.exit(1);
        }
    }

    private static boolean checkMainArguments(ArgumentResolver resolver) {
        return resolver.containsArgument(INPUT_DIR_PARAM) &
                resolver.containsArgument(OUTPUT_DIR_PARAM) &
                resolver.containsArgument(POST_OFFICE_NUM_PARAM) &
                resolver.containsArgument(POST_INDEX_FILE_PARAM);
    }

    private static void showUsage() {
        System.out.printf("Usage: java partpostconverter -%s input_directory " +
                        "-%s output_directory -%s post_index_file " +
                        "-%s default_index%n",
                INPUT_DIR_PARAM, OUTPUT_DIR_PARAM, POST_INDEX_FILE_PARAM,
                POST_OFFICE_NUM_PARAM);
    }

    private static void showError(Throwable throwable) {
        StringBuilder message = new StringBuilder("ERROR");

        do {
            message.append(": ").append(throwable.getMessage());
            throwable = throwable.getCause();
        } while (throwable != null);
        System.err.println(message);
    }
}
