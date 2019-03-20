package com.github.svserge89.partpostconverter.io;

import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

class ZipExtractor {
    private Path archive;

    ZipExtractor(Path archive) {
        this.archive = archive;
    }

    Map<String, byte[]> getFileBytes() throws IOException {
        Map<String, byte[]> result = new HashMap<>();

        try (ZipInputStream zipInputStream = new ZipInputStream(Files.newInputStream(archive))) {
            ZipEntry zipEntry;

            while ((zipEntry = zipInputStream.getNextEntry()) != null) {
                if (!zipEntry.isDirectory()) {
                    String fileName = zipEntry.getName();
                    byte[] bytes = IOUtils.toByteArray(zipInputStream);

                    result.put(fileName, bytes);
                }
                zipInputStream.closeEntry();
            }
        }

        return result;
    }
}
