package com.github.svserge89.partpostconverter.io;

import com.github.svserge89.partpostconverter.corrector.FileCorrector;
import com.github.svserge89.partpostconverter.exception.DirectoryWalkerException;
import com.github.svserge89.partpostconverter.exception.FileCorrectorException;
import com.github.svserge89.partpostconverter.resolver.RegionResolver;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DirectoryWalker {
    private static final Logger log = LoggerFactory.getLogger(DirectoryWalker.class);

    private Path inputDirectory;
    private Path outputDirectory;
    private List<Path> archiveList = new ArrayList<>();

    public DirectoryWalker(Path inputDirectory, Path outputDirectory)
            throws IOException {
        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;
        checkInputDirectory();
        checkOutputDirectory();
        walkDirectory();
    }

    public void runCorrector(RegionResolver regionResolver, int defaultPostOfficeNumber) {
        for (Path archive : archiveList) {
            try {
                archiveWorker(archive, regionResolver, defaultPostOfficeNumber);

                log.info("Archive file \"{}\" processed", archive.getFileName());
            } catch (Exception e) {
                log.error("Incorrect archive file \"{}\"", archive.getFileName());

                throw new FileCorrectorException("Incorrect archive file " +
                        archive.getFileName(), e);
            }

        }
    }

    private void walkDirectory() throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDirectory)) {
            stream.forEach(path -> {
                if (Files.isReadable(path) && Files.isRegularFile(path)) {
                    if (path.toString().endsWith(".zip")) {
                        log.info("Found archive file \"{}\"", path.getFileName());

                        archiveList.add(path);
                    }
                }
            });
        }

        if (archiveList.isEmpty()) {
            log.error("\"{}\" - is not contain zip files");

            throw new DirectoryWalkerException(inputDirectory + " is not contain zip files");
        }
    }

    private void archiveWorker(Path archive, RegionResolver regionResolver,
                               int defaultPostOfficeNumber) throws IOException {
        ZipExtractor extractor = new ZipExtractor(archive);
        Map<String, byte[]> fileBytes = extractor.getFileBytes();

        for (Map.Entry<String, byte[]> entry : fileBytes.entrySet()) {

            Path outputFile = outputDirectory.resolve(Paths.get(entry.getKey()).getFileName());

            if (entry.getKey().endsWith(".txt")) {
                log.info("Starting correction for file \"{}\"", outputFile.getFileName());

                FileCorrector converter = new FileCorrector(entry.getValue(), regionResolver,
                        defaultPostOfficeNumber);
                converter.writeToFile(outputFile);

                log.info("File \"{}\" extracted from \"{}\", corrected and written to \"{}\"",
                        outputFile.getFileName(), archive.getFileName(), outputDirectory);
            } else {
                IOUtils.write(entry.getValue(), Files.newOutputStream(outputFile));

                log.info("File \"{}\" extracted from \"{}\" and written to \"{}\"",
                        outputFile.getFileName(), archive.getFileName(), outputDirectory);
            }
        }
    }

    private void checkInputDirectory() {
        if (!Files.exists(inputDirectory)) {
            log.error("\"{}\" - is not exist", inputDirectory);

            throw new DirectoryWalkerException(inputDirectory + " is not exist");
        }
        if (!Files.isDirectory(inputDirectory)) {
            log.error("\"{}\" - is not a directory", inputDirectory);

            throw new DirectoryWalkerException(inputDirectory + " is not a directory");
        }
    }

    private void checkOutputDirectory() {
        if (Files.exists(outputDirectory)) {
            if (!Files.isDirectory(outputDirectory)) {
                log.error("\"{}\" - is not a directory", outputDirectory);

                throw new DirectoryWalkerException(outputDirectory + " is not a directory");
            }

            if (!Files.isWritable(outputDirectory)) {
                log.error("\"{}\" - is not writable", outputDirectory);

                throw new DirectoryWalkerException(outputDirectory + " is not writable");
            }
        } else {
            try {
                Files.createDirectories(outputDirectory);

                log.info("Created output directory \"{}\"", outputDirectory);
            } catch (Exception e) {
                log.error("Can't create directory \"{}\" - {}",outputDirectory, e.getMessage());

                throw new DirectoryWalkerException("Can't creat directory " +
                        outputDirectory.getFileName(), e);
            }
        }
    }
}
