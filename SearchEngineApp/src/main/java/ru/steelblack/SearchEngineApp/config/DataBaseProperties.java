package ru.steelblack.SearchEngineApp.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


import java.util.HashMap;

@Configuration
@ConfigurationProperties(prefix = "spring")
public class DataBaseProperties {

    private HashMap<String, String> dataSource = new HashMap<>();

    public HashMap<String, String> getDataSource() {
        return dataSource;
    }

    public void setDataSource(HashMap<String, String> dataSource) {
        this.dataSource = dataSource;
    }
}
