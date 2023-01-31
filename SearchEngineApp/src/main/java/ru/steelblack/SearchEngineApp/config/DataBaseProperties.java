package ru.steelblack.SearchEngineApp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


import java.util.HashMap;

@Configuration
@ConfigurationProperties(prefix = "spring")
@Getter
@Setter
public class DataBaseProperties {

    private HashMap<String, String> dataSource = new HashMap<>();

}
