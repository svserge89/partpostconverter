package com.github.svserge89;

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
    private String defaultRegion;

    public RegionResolver(Path path, String defaultRegion) throws IOException {
        this.path = path;
        this.defaultRegion = defaultRegion;
        initMap();
    }

    public String getRegion(int postOfficeNumber) {
        return regionMap.getOrDefault(postOfficeNumber, defaultRegion);
    }

    private void initMap() throws IOException {
        try (DBFReader reader = new DBFReader(Files.newInputStream(path), CP_866)) {
            for (DBFRow row = reader.nextRow(); row != null; row = reader.nextRow()) {
                int postOfficeNumber = Integer.parseInt(row.getString("Index"));
                String region = row.getString("Region");
                regionMap.put(postOfficeNumber, region);
            }
        }
    }
}
