package ru.steelblack.SearchEngineApp.services.indexingService;

import ru.steelblack.SearchEngineApp.pageDTO.IndexingDTO.IndexingResponse;
import ru.steelblack.SearchEngineApp.pageDTO.statictics.StatisticsResponse;

public interface StatisticService {
    StatisticsResponse getStatistics();

    IndexingResponse getStartIndexingSites();

    IndexingResponse getStartIndexingPage(String url);

    IndexingResponse terminate();




}
