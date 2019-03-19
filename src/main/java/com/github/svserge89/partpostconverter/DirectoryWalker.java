package com.github.svserge89.partpostconverter;

import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class DirectoryWalker {
    private Path inputDirectory;
    private Path outputDirectory;
    private List<Path> fileList = new ArrayList<>();

    public DirectoryWalker(Path inputDirectory, Path outputDirectory)
            throws IOException {
        this.inputDirectory = inputDirectory;
        this.outputDirectory = outputDirectory;
        checkInputDirectory();
        checkOutputDirectory();
        walkDirectory();
    }

    public void runConverter(RegionResolver regionResolver,
                             int defaultPostOfficeNumber) throws IOException {
        for (Path file : fileList) {
            FileConverter fileConverter = new FileConverter(file,
                    regionResolver, defaultPostOfficeNumber);
            Path outputFile = outputDirectory.resolve(file.getFileName());
            fileConverter.writeToFile(outputFile);
        }
    }

    private void walkDirectory() throws IOException {
        try (DirectoryStream<Path> stream =
                     Files.newDirectoryStream(inputDirectory)) {
            stream.forEach(path -> {
                if (Files.isReadable(path) && Files.isRegularFile(path) &&
                        path.toString().endsWith(".txt")) {
                    fileList.add(path);
                }
            });
        }
//        fileList.forEach(System.out::println);
        if (fileList.isEmpty()) {
            throw new IOException(inputDirectory +
                    "is not contain correct files");
        }
    }

    private void checkInputDirectory() throws IOException {
        if (!Files.exists(inputDirectory)) {
            throw new IOException(inputDirectory + "is not exist");
        }
        if (!Files.isDirectory(inputDirectory)) {
            throw new IOException(inputDirectory + "is not a directory");
        }
    }

    private void checkOutputDirectory() throws IOException {
        if (Files.exists(outputDirectory) &&
                !Files.isDirectory(outputDirectory)) {
            throw new IOException(outputDirectory + " is not a directory");
        }
        if (!Files.exists(outputDirectory)) {
            Files.createDirectories(outputDirectory);
        }
    }
}
