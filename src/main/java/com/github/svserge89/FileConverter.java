package com.github.svserge89;

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

    public FileConverter(Path path, RegionResolver regionResolver) {
        this.path = path;
        this.regionResolver = regionResolver;
    }

    private String getStringWithRegions() throws IOException {
        List<String> lines = Files.readAllLines(path, CP_866);
        StringBuilder result = new StringBuilder();

        lines.forEach(line -> {
            if (line.startsWith("Barcode")) {
                result.append(line);
            } else {
                String[] tokens = line.split(REGEX, -1);

                tokens[REGION_INDEX] = regionResolver.getRegion(
                        Integer.parseInt(tokens[POST_OFFICE_INDEX]));
                String changedLine = String.join(DELIMITER,
                                                 Arrays.asList(tokens));
                result.append(changedLine);
            }
            result.append(CRLF);
        });
        return result.toString();
    }

    public void writeToFile(Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, CP_866)) {
            writer.write(getStringWithRegions());
        }
    }
}
