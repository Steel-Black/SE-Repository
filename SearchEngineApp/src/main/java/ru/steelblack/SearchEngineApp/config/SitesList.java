package ru.steelblack.SearchEngineApp.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;
import ru.steelblack.SearchEngineApp.models.Site;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "indexing-settings")
@Setter
@Getter
public class SitesList {
    private List<Site> sites;
}
