package com.github.svserge89.partpostconverter;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class FileConverter {
    private static final Charset CP_866 = Charset.forName("cp866");
    private static final int REGION_INDEX = 11;
    private static final int POST_OFFICE_INDEX = 10;
    private static final String REGEX = "\\|";
    private static final String DELIMITER = "|";
    private static final String CRLF = "\r\n";

    private Path path;
    private RegionResolver regionResolver;
    private int defaultPostOfficeNumber;

    public FileConverter(Path path, RegionResolver regionResolver,
                         int defaultPostOfficeNumber) {
        this.path = path;
        this.regionResolver = regionResolver;
        this.defaultPostOfficeNumber = defaultPostOfficeNumber;
    }

    private String getStringWithRegions() throws IOException {
        List<String> lines = Files.readAllLines(path, CP_866);
        StringBuilder result = new StringBuilder();

        lines.forEach(line -> lineCorrector(line, result));
        return result.toString();
    }

    private void lineCorrector(String line, StringBuilder result) {
        if (line.startsWith("Barcode")) {
            result.append(line);
        } else {
            String[] tokens = line.split(REGEX, -1);

            int postOfficeIndex = Integer.parseInt(tokens[POST_OFFICE_INDEX]);

            if (regionResolver.isCorrectPostOffice(postOfficeIndex)) {
                tokens[REGION_INDEX] =
                        regionResolver.getRegion(postOfficeIndex);
            } else {
                tokens[POST_OFFICE_INDEX] =
                        Integer.toString(defaultPostOfficeNumber);
                tokens[REGION_INDEX] =
                        regionResolver.getRegion(defaultPostOfficeNumber);
            }

            String changedLine = String.join(DELIMITER,
                    Arrays.asList(tokens));
            result.append(changedLine);
        }
        result.append(CRLF);
    }

    public void writeToFile(Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, CP_866)) {
            writer.write(getStringWithRegions());
        }
    }
}
