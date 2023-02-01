package ru.steelblack.SearchEngineApp.services.indexingService;

import ru.steelblack.SearchEngineApp.dto.IndexingDTO.IndexingResponse;
import ru.steelblack.SearchEngineApp.dto.statictics.StatisticsResponse;

public interface StatisticService {
    StatisticsResponse getStatistics();

    IndexingResponse getStartIndexingSites();

    IndexingResponse getStartIndexingPage(String url);

    IndexingResponse terminate();




}
