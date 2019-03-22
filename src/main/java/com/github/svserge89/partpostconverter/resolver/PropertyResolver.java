package com.github.svserge89.partpostconverter.resolver;

import com.github.svserge89.partpostconverter.exception.PropertyResolverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

public class PropertyResolver {
    private static final Logger log = LoggerFactory.getLogger(PropertyResolver.class);

    private Properties properties = new Properties();

    public PropertyResolver() {
        try {
            properties.load(ClassLoader.getSystemResourceAsStream("default.properties"));
        } catch (Exception e) {
            log.error("Can't load default properties", e);

            throw new PropertyResolverException("Can't load default properties", e);
        }
    }

    public PropertyResolver(Path path) {
        this();

        Properties defaultProperties = properties;
        properties = new Properties(defaultProperties);

        try {
            properties.load(Files.newInputStream(path));

            log.info("Using property file \"{}\"", path.getFileName());
        } catch (Exception e) {
            log.error("Can't load properties from file \"{}\"", path.getFileName());

            throw new PropertyResolverException("Can't load properties from file " +
                    path.getFileName(), e);
        }
    }

    public boolean containsProperty(String property) {
        return properties.containsKey(property);
    }

    public String getProperty(String property) {
        return properties.getProperty(property);
    }
}
