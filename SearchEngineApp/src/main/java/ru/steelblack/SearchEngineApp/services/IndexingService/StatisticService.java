package ru.steelblack.SearchEngineApp.services.IndexingService;

import ru.steelblack.SearchEngineApp.pageDTO.IndexingDTO.IndexingResponse;
import ru.steelblack.SearchEngineApp.pageDTO.statictics.StatisticsResponse;

public interface StatisticService {
    StatisticsResponse getStatistics();

    IndexingResponse indexingSites();

    IndexingResponse indexingPage(String url);

    IndexingResponse terminate();




}
