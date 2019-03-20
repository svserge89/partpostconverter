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
    private static final int RECIPIENT_LENGTH = 255;
    private static final int RECIPIENT_INDEX = 7;
    private static final String REGEX = "\\|";
    private static final String DELIMITER = "|";
    private static final String CRLF = "\r\n";

    private Path path;
    private RegionResolver regionResolver;
    private int defaultPostOfficeNumber;
    private int tokensLength = 0;

    public FileConverter(Path path, RegionResolver regionResolver,
                         int defaultPostOfficeNumber) {
        this.path = path;
        this.regionResolver = regionResolver;
        this.defaultPostOfficeNumber = defaultPostOfficeNumber;
    }

    private String getStringWithRegions() throws IOException {
        List<String> lines = Files.readAllLines(path, CP_866);
        StringBuilder result = new StringBuilder();
        try {
            lineCorrector(lines, result);
        } catch (ArrayIndexOutOfBoundsException |
                IllegalArgumentException exception) {
            throw new IOException(path + " is incorrect file", exception);
        }
        return result.toString();
    }

    private void lineCorrector(List<String> lines, StringBuilder result) {
        for (int i = 0; i < lines.size(); ++i) {
            String line = lines.get(i);
            String[] tokens = line.split(REGEX, -1);

            if (line.startsWith("Barcode")) {
                result.append(line);
                tokensLength = tokens.length;
            } else {
                while (tokens.length < tokensLength) {
                    String nextLine = lines.get(++i);
                    while (nextLine.trim().isEmpty()) {
                        nextLine = lines.get(++i);
                    }
                    String[] nextLineTokens = nextLine.split(REGEX, -1);
                    int fixedLineSize =
                            tokens.length + nextLineTokens.length - 1;
                    if (fixedLineSize > tokensLength) {
                        throw new IllegalArgumentException("Can't parse file");
                    }
                    String[] fixedTokens = new String[fixedLineSize];
                    int j;

                    for (j = 0; j < tokens.length; ++j) {
                        fixedTokens[j] = tokens[j];
                    }

                    fixedTokens[j - 1] = fixedTokens[j - 1] + " " +
                            nextLineTokens[0];

                    for (int k = 1; k < nextLineTokens.length &&
                            j < fixedTokens.length; ++k, ++j) {
                        fixedTokens[j] = nextLineTokens[k];
                    }

                    tokens = fixedTokens;
                }

                correctTokens(tokens);
                truncateToken(tokens, RECIPIENT_INDEX, RECIPIENT_LENGTH);

                int postOfficeIndex =
                        Integer.parseInt(tokens[POST_OFFICE_INDEX]);

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
    }

    private static void correctTokens(String[] tokens) {
        for (int i = 0; i < tokens.length; ++i) {
            tokens[i] = tokens[i].trim().replaceAll("\\s+", " ");
        }
    }

    private static void truncateToken(String[] tokens, int index, int maxLength) {
        if (tokens[index].length() > maxLength) {
            tokens[index] = tokens[index].substring(0, maxLength);
        }
    }

    public void writeToFile(Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, CP_866)) {
            writer.write(getStringWithRegions());
        }
    }
}
