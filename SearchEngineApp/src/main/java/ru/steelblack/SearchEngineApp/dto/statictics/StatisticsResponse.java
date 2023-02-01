package ru.steelblack.SearchEngineApp.dto.statictics;

import lombok.Data;

@Data
public class StatisticsResponse {

    private boolean result;

    private StatisticsData statistics;

}
