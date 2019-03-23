package com.github.svserge89.partpostconverter.corrector;

import com.github.svserge89.partpostconverter.exception.FileCorrectorException;
import com.github.svserge89.partpostconverter.resolver.RegionResolver;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private static final int BARCODE_INDEX = 0;
    private static final String BARCODE_FIELD_NAME = "Barcode";
    private static final String DELIMITER_REGEX = "\\|";
    private static final String DELIMITER = "|";
    private static final String WHITESPACES_REGEX = "\\s\\s+";
    private static final String COMMAS_REGEX = ",\\s?,(\\s?,)*";
    private static final String START_AND_END_COMMAS_REGEX = "(\\s?,$)|(^,\\s?)";
    private static final String NON_DIGIT_PATTERN = "\\D+";

    public static final String POST_OFFICE_NUMBER_PATTERN = "^\\d{6}$";

    private static Logger log = LoggerFactory.getLogger(FileCorrector.class);

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
            String[] tokens = line.split(DELIMITER_REGEX, -1);

            if (line.startsWith(BARCODE_FIELD_NAME)) {
                result.append(line);
                tokensLength = tokens.length;
            } else {
                while (tokens.length < tokensLength) {
                    log.warn("Incorrect line separators in line: {}", i + 1);

                    String nextLine = lines.get(++i);

                    while (nextLine.trim().isEmpty()) {
                        log.warn("Line {} is empty", i + 1);

                        nextLine = lines.get(++i);
                    }

                    String[] nextLineTokens = nextLine.split(DELIMITER_REGEX, -1);
                    int fixedLineSize = tokens.length + nextLineTokens.length - 1;

                    if (fixedLineSize > tokensLength) {
                        log.error("Can't parse file in line: {}", i);

                        throw new FileCorrectorException("Can't parse file in line: " + i);
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

                    if (fixedTokens.length == tokensLength) {
                        log.warn("\"{}\" - incorrect line separators removed",
                                fixedTokens[BARCODE_INDEX]);
                    }
                }

                removeRedundantWhiteSpaceAndCommas(tokens, RECIPIENT_INDEX, ADDRESS_INDEX);
                truncateRecipient(tokens);
                fixPostOfficeNumber(tokens, Integer.toString(defaultPostOfficeNumber));

                int postOfficeNumber = Integer.parseInt(tokens[POST_OFFICE_INDEX]);

                if (regionResolver.numberIsExist(postOfficeNumber)) {
                    tokens[REGION_INDEX] = regionResolver.getRegion(postOfficeNumber);
                } else {
                    log.warn("\"{}\" - can't resolve index \"{}\"", tokens[BARCODE_INDEX],
                            tokens[REGION_INDEX]);

                    tokens[POST_OFFICE_INDEX] = Integer.toString(defaultPostOfficeNumber);

                    log.warn("\"{}\" - using default index \"{}\"", tokens[BARCODE_INDEX],
                            tokens[REGION_INDEX]);

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
            String token = tokens[i].trim()
                    .replaceAll(WHITESPACES_REGEX, " ")
                    .replaceAll(COMMAS_REGEX, ",")
                    .replaceAll(START_AND_END_COMMAS_REGEX, "");

            if (!token.equals(tokens[i])) {
                log.warn("\"{}\" - incorrect field \"{}\"", tokens[BARCODE_INDEX], tokens[i]);

                tokens[i] = token;

                log.warn("\"{}\" - field replaced to \"{}\"", tokens[BARCODE_INDEX], token);
            }
        }
    }

    private static void truncateRecipient(String[] tokens) {
        if (tokens[RECIPIENT_INDEX].length() > RECIPIENT_LENGTH) {
            log.warn("\"{}\" - recipient \"{}\" field length > {} characters",
                    tokens[BARCODE_INDEX], tokens[RECIPIENT_INDEX], RECIPIENT_LENGTH);

            tokens[RECIPIENT_INDEX] = tokens[RECIPIENT_INDEX].substring(0, RECIPIENT_LENGTH);

            log.warn("\"{}\" - recipient truncated to \"{}\"", tokens[BARCODE_INDEX],
                    tokens[RECIPIENT_INDEX]);
        }
    }

    private static void fixPostOfficeNumber(String[] tokens, String defaultPostOffice) {
        String postOffice = tokens[POST_OFFICE_INDEX];

        if (!postOffice.matches(POST_OFFICE_NUMBER_PATTERN)) {
            log.warn("\"{}\" - index \"{}\" is incorrect", tokens[BARCODE_INDEX],
                    tokens[POST_OFFICE_INDEX]);

            postOffice = postOffice.trim().replaceAll(NON_DIGIT_PATTERN, "");
            if (!postOffice.matches(POST_OFFICE_NUMBER_PATTERN)) {
                tokens[POST_OFFICE_INDEX] = defaultPostOffice;

                log.warn("\"{}\" - using default index \"{}\"", tokens[BARCODE_INDEX],
                        defaultPostOffice);
            } else {
                tokens[POST_OFFICE_INDEX] = postOffice;

                log.warn("\"{}\" - index corrected to \"{}\"", tokens[BARCODE_INDEX], postOffice);
            }
        }
    }

    public void writeToFile(Path path) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(path, CP_866)) {
            writer.write(getStringWithRegions());
        }
    }
}
