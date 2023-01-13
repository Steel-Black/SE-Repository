package ru.steelblack.SearchEngineApp.services.IndexingService;

import ru.steelblack.SearchEngineApp.pageDTO.IndexingDTO.IndexingResponse;
import ru.steelblack.SearchEngineApp.pageDTO.statictics.StatisticsResponse;
import ru.steelblack.SearchEngineApp.util.SearchEngineException.RepeatedRequestException;

public interface StatisticService {
    StatisticsResponse getStatistics();

    IndexingResponse getStartIndexing();

    IndexingResponse indexingPage(String url);

    IndexingResponse terminate();




}
