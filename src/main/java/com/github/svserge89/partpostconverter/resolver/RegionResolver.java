package com.github.svserge89.partpostconverter.resolver;

import com.github.svserge89.partpostconverter.exception.RegionResolverException;
import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFRow;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

public class RegionResolver {
    private static final Charset CP_866 = Charset.forName("cp866");

    private Map<Integer, String> regionMap = new TreeMap<>();
    private Path path;

    public RegionResolver(Path path) {
        this.path = path;
        try {
            initMap();
        } catch (Exception e) {
            throw new RegionResolverException("Incorrect Post index DBF file", e);
        }
    }

    public boolean numberIsExist(int postOfficeNumber) {
        return regionMap.containsKey(postOfficeNumber);
    }

    public String getRegion(int postOfficeNumber) {
        return regionMap.get(postOfficeNumber);
    }

    private void initMap() throws IOException {
        try (DBFReader reader = new DBFReader(Files.newInputStream(path), CP_866)) {
            for (DBFRow row = reader.nextRow(); row != null;
                 row = reader.nextRow()) {
                int postOfficeNumber =
                        Integer.parseInt(row.getString("Index"));
                String region = row.getString("Region");
                regionMap.put(postOfficeNumber, region);
            }
        }
    }
}