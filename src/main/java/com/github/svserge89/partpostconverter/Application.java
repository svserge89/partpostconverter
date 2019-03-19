package com.github.svserge89.partpostconverter;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Application {
//    private static final String TEST_FILE = System.getProperty("user.home") + "/Development/test.txt";
//    private static final String OUT_FILE = System.getProperty("user.home") + "/Development/out.txt";
    private static final String DBF_FILE = System.getProperty("user.home") + "/Development/PIndx05.dbf";
    private static final String INPUT_DIRECTORY = System.getProperty("user.home") + "/Development/input";
    private static final String OUTPUT_DIRECTORY = System.getProperty("user.home") + "/Development/output";

    public static void main(String[] args) throws IOException {
        Path pindxFile = Paths.get(DBF_FILE);
//        Path testFile = Paths.get(TEST_FILE);
//        Path outFile = Paths.get(OUT_FILE);
        Path inputDirectory = Paths.get(INPUT_DIRECTORY);
        Path outputDirectory = Paths.get(OUTPUT_DIRECTORY);
//
        RegionResolver regionResolver = new RegionResolver(pindxFile);
//        FileConverter fileConverter = new FileConverter(testFile, regionResolver, 346480);
//        fileConverter.writeToFile(outFile);
        DirectoryWalker directoryWalker = new DirectoryWalker(inputDirectory, outputDirectory);
        directoryWalker.runConverter(regionResolver, 346480);
    }
}
