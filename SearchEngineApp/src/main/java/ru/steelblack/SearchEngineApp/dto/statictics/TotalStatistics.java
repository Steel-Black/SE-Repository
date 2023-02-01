package ru.steelblack.SearchEngineApp.dto.statictics;

import lombok.Data;

@Data
public class TotalStatistics {
    private int sites;
    private int pages;
    private int lemmas;
    private boolean indexing;

}
