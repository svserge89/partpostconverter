package com.github.svserge89.partpostconverter.resolver;

import com.github.svserge89.partpostconverter.exception.RegionResolverException;
import com.linuxense.javadbf.DBFReader;
import com.linuxense.javadbf.DBFRow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.TreeMap;

public class RegionResolver {
    private static final Logger log = LoggerFactory.getLogger(RegionResolver.class);
    private static final Charset CP_866 = Charset.forName("cp866");

    private static final String INDEX_COLUMN = "Index";
    private static final String REGION_COLUMN = "Region";

    private Map<Integer, String> regionMap = new TreeMap<>();
    private Path path;

    public RegionResolver(Path path) {
        this.path = path;
        try {
            initMap();

            log.info("Post index DBF File \"{}\" successfully loaded", path.getFileName());
        } catch (Exception e) {
            log.error("Incorrect Post index DBF file \"{}\"", path.getFileName());

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
                        Integer.parseInt(row.getString(INDEX_COLUMN));
                String region = row.getString(REGION_COLUMN);
                regionMap.put(postOfficeNumber, region);
            }
        }
    }
}
