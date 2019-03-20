package com.github.svserge89.partpostconverter.resolver;

import com.github.svserge89.partpostconverter.exception.ArgumentResolverException;

import java.util.HashMap;
import java.util.Map;

public class ArgumentResolver {
    private String[] args;
    private Map<String, String> argumentsMap = new HashMap<>();

    public ArgumentResolver(String[] args) {
        this.args = args;
        try {
            parseArguments();
        } catch (Exception e) {
            throw new ArgumentResolverException("Incorrect commandline arguments", e);
        }
    }

    private void parseArguments() {
        for (int i = 0; i < args.length - 1; ++i) {
            if (args[i].startsWith("-")) {
                String key = args[i].substring(1);
                if (args[i + 1].startsWith("-")) {
                    throw new IllegalArgumentException(args[i + 1] + " is incorrect argument");
                } else {
                    argumentsMap.put(key, args[i + 1]);
                    ++i;
                }
            }
        }
    }

    public boolean containsArgument(String argument) {
        return argumentsMap.containsKey(argument);
    }

    public String getValue(String argument) {
        return argumentsMap.get(argument);
    }
}
