package com.github.svserge89.partpostconverter.corrector;

import com.github.svserge89.partpostconverter.exception.FileCorrectorException;
import com.github.svserge89.partpostconverter.resolver.RegionResolver;
import org.apache.commons.io.IOUtils;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class FileCorrector {
    private static final Charset CP_866 = Charset.forName("cp866");
    private static final int REGION_INDEX = 11;
    private static final int POST_OFFICE_INDEX = 10;
    private static final int ADDRESS_INDEX = 15;
    private static final int RECIPIENT_LENGTH = 60;
    private static final int RECIPIENT_INDEX = 7;
    private static final String REGEX = "\\|";
    private static final String DELIMITER = "|";

    private RegionResolver regionResolver;
    private byte[] bytes;
    private int defaultPostOfficeNumber;
    private int tokensLength = 0;

    public FileCorrector(byte[] bytes, RegionResolver regionResolver,
                         int defaultPostOfficeNumber) {
        this.bytes = bytes;
        this.regionResolver = regionResolver;
        this.defaultPostOfficeNumber = defaultPostOfficeNumber;
    }

    private String getStringWithRegions() throws IOException {
        ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
        List<String> lines = IOUtils.readLines(stream, CP_866);

        stream.close();

        StringBuilder result = new StringBuilder();

        lineCorrector(lines, result);

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
                    int fixedLineSize = tokens.length + nextLineTokens.length - 1;

                    if (fixedLineSize > tokensLength) {
                        throw new FileCorrectorException("Incorrect line:" + (i - 1));
                    }

                    String[] fixedTokens = new String[fixedLineSize];
                    int j;

                    for (j = 0; j < tokens.length; ++j) {
                        fixedTokens[j] = tokens[j];
                    }

                    fixedTokens[j - 1] = fixedTokens[j - 1] + " " + nextLineTokens[0];

                    for (int k = 1; k < nextLineTokens.length && j < fixedTokens.length;
                         ++k, ++j) {
                        fixedTokens[j] = nextLineTokens[k];
                    }

                    tokens = fixedTokens;
                }

                removeRedundantWhiteSpaceAndCommas(tokens, RECIPIENT_INDEX, ADDRESS_INDEX);
                truncateRecipient(tokens);

                int postOfficeIndex = Integer.parseInt(tokens[POST_OFFICE_INDEX]);

                if (regionResolver.numberIsExist(postOfficeIndex)) {
                    tokens[REGION_INDEX] = regionResolver.getRegion(postOfficeIndex);
                } else {
                    tokens[POST_OFFICE_INDEX] = Integer.toString(defaultPostOfficeNumber);
                    tokens[REGION_INDEX] = regionResolver.getRegion(defaultPostOfficeNumber);
                }

                String changedLine = String.join(DELIMITER, Arrays.asList(tokens));

                result.append(changedLine);
            }
            result.append(IOUtils.LINE_SEPARATOR_WINDOWS);
        }
    }

    private static void removeRedundantWhiteSpaceAndCommas(String[] tokens, int... index) {
        for (int i : index) {
            tokens[i] = tokens[i].trim()
                    .replaceAll("\\s\\s+", " ")
                    .replaceAll(",\\s?,(\\s?,)*", ",")
                    .replaceAll("(\\s?,$)|(^,\\s?)", "");
        }
    }

    private static void truncateRecipient(String[] tokens) {
        if (tokens[RECIPIENT_INDEX].length() > RECIPIENT_LENGTH) {
            tokens[RECIPIENT_INDEX] = tokens[RECIPIENT_INDEX].substring(0, RECIPIENT_LENGTH);
        }
    }

    public void writeToFile(Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, CP_866)) {
            writer.write(getStringWithRegions());
        }
    }
}
