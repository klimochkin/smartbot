package com.kobi.smartbot.util;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class DataLoader {


    private final ResourceLoader resourceLoader;
    private final Environment environment;


    @Autowired
    public DataLoader(ResourceLoader resourceLoader, Environment environment) {
        this.resourceLoader = resourceLoader;
        this.environment = environment;
    }

    public List<String> getSetting(String settingName) {
        List<String> items = new ArrayList<>();
        String settingStr = environment.getProperty(settingName);
        if (settingStr != null && !settingStr.isEmpty()) {
            items = Arrays.asList(settingStr.split(";"));
        }
        return items;
    }

    public List<String> getIds(List<String> items) {
        List<String> ids;
        ids = items.stream().map(item -> item.split("=")[0]).collect(Collectors.toList());
        return ids;
    }


    public List<String> loadFileLines(String filePath) throws IOException {
        Resource resource = resourceLoader.getResource(filePath);
        List<String> lines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(resource.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        }
        return lines;
    }

}