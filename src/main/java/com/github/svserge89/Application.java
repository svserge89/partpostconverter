package com.github.svserge89;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Application {
    private static final String TEST_FILE = System.getProperty("user.home") + "/Development/test.txt";
    private static final String OUT_FILE = System.getProperty("user.home") + "/Development/out.txt";
    private static final String DBF_FILE = System.getProperty("user.home") + "/Development/PIndx05.dbf";

    public static void main(String[] args) throws IOException {
        Path pindxFile = Paths.get(DBF_FILE);
        Path testFile = Paths.get(TEST_FILE);
        Path outFile = Paths.get(OUT_FILE);

        RegionResolver regionResolver = new RegionResolver(pindxFile, "TEST");
        FileConverter fileConverter = new FileConverter(testFile, regionResolver);
        fileConverter.writeToFile(outFile);
    }
}
