package ru.steelblack.SearchEngineApp.pageDTO.statictics;

import lombok.Data;

import java.util.List;

@Data
public class StatisticsData {
    private TotalStatistics total;
    private List<DetailedStatisticsItem> detailed;

}
