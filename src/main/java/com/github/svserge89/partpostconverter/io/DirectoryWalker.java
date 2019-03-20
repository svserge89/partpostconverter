package com.github.svserge89.partpostconverter.io;

import com.github.svserge89.partpostconverter.corrector.FileCorrector;
import com.github.svserge89.partpostconverter.exception.DirectoryWalkerException;
import com.github.svserge89.partpostconverter.exception.FileCorrectorException;
import com.github.svserge89.partpostconverter.resolver.RegionResolver;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DirectoryWalker {
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
            } catch (Exception e) {
                throw new FileCorrectorException("Incorrect archive " + archive.getFileName(), e);
            }

        }
    }

    private void walkDirectory() throws IOException {
        try (DirectoryStream<Path> stream = Files.newDirectoryStream(inputDirectory)) {
            stream.forEach(path -> {
                if (Files.isReadable(path) && Files.isRegularFile(path)) {
                    if (path.toString().endsWith(".zip")) {
                        archiveList.add(path);
                    }
                }
            });
        }

        if (archiveList.isEmpty()) {
            throw new IOException(inputDirectory + " is not contain zip files");
        }
    }

    private void archiveWorker(Path archive, RegionResolver regionResolver,
                               int defaultPostOfficeNumber) throws IOException {
        ZipExtractor extractor = new ZipExtractor(archive);
        Map<String, byte[]> fileBytes = extractor.getFileBytes();

        for (Map.Entry<String, byte[]> entry : fileBytes.entrySet()) {

            Path outputFile = outputDirectory.resolve(Paths.get(entry.getKey()).getFileName());

            if (entry.getKey().endsWith(".txt")) {
                FileCorrector converter = new FileCorrector(entry.getValue(), regionResolver,
                        defaultPostOfficeNumber);
                converter.writeToFile(outputFile);
            } else {
                IOUtils.write(entry.getValue(), Files.newOutputStream(outputFile));
            }
        }
    }

    private void checkInputDirectory() {
        if (!Files.exists(inputDirectory)) {
            throw new DirectoryWalkerException(inputDirectory + " is not exist");
        }
        if (!Files.isDirectory(inputDirectory)) {
            throw new DirectoryWalkerException(inputDirectory + " is not a directory");
        }
    }

    private void checkOutputDirectory() {
        if (Files.exists(outputDirectory)) {
            if (!Files.isDirectory(outputDirectory)) {
                throw new DirectoryWalkerException(outputDirectory + " is not a directory");
            }

            if (!Files.isWritable(outputDirectory)) {
                throw new DirectoryWalkerException(inputDirectory + " is not writable");
            }
        } else {
            try {
                Files.createDirectories(outputDirectory);
            } catch (Exception e) {
                throw new DirectoryWalkerException("Can't creat directory " +
                        outputDirectory.getFileName(), e);
            }
        }
    }
}
