package ru.steelblack.SearchEngineApp.pageDTO.statictics;

import lombok.Data;

@Data
public class StatisticsResponse {

    private boolean result;

    private StatisticsData statistics;

}
