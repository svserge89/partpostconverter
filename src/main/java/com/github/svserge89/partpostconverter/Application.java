package com.github.svserge89.partpostconverter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Application {
    private static final String INPUT_DIR_PARAM = "input";
    private static final String OUTPUT_DIR_PARAM = "output";
    private static final String DEFAULT_POST_OFFICE_NUM = "office-number";
    private static final String DBF_FILE = "post-index-db";

    public static void main(String[] args) throws IOException {
        try {
            ArgumentResolver argumentResolver = new ArgumentResolver(args);

            if (!checkMainArguments(argumentResolver)) {
                System.err.println("ERROR: Incorrect arguments");
                showUsage();
                System.exit(1);
            }

            Path inputDirectory =
                    Paths.get(argumentResolver.getValue(INPUT_DIR_PARAM));
            Path outputDirectory =
                    Paths.get(argumentResolver.getValue(OUTPUT_DIR_PARAM));
            Path dbfFile = Paths.get(argumentResolver.getValue(DBF_FILE));

            int defaultPostOfficeNumber = Integer.parseInt(
                    argumentResolver.getValue(DEFAULT_POST_OFFICE_NUM));

            DirectoryWalker directoryWalker =
                    new DirectoryWalker(inputDirectory, outputDirectory);

            RegionResolver regionResolver = new RegionResolver(dbfFile);

            directoryWalker.runConverter(regionResolver, defaultPostOfficeNumber);
        } catch (Exception exception) {
            System.err.println("ERROR: " + exception.getMessage());
            System.exit(1);
        }
    }

    private static boolean checkMainArguments(ArgumentResolver resolver) {
        return resolver.containsArgument(INPUT_DIR_PARAM) &
                resolver.containsArgument(OUTPUT_DIR_PARAM) &
                resolver.containsArgument(DEFAULT_POST_OFFICE_NUM) &
                resolver.containsArgument(DBF_FILE);
    }

    public static void showUsage() {
        System.out.printf("Usage: java partpostconverter -%s input_directory " +
                        "-%s output_directory -%s post_index_file " +
                        "-%s default_index%n",
                INPUT_DIR_PARAM, OUTPUT_DIR_PARAM, DBF_FILE,
                DEFAULT_POST_OFFICE_NUM);
    }
}
