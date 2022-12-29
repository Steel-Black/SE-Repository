package ru.steelblack.SearchEngineApp.services.searchService;

import ru.steelblack.SearchEngineApp.pageDTO.SearchDTO.ResponseData;

public interface SearchService {

    ResponseData searchPages(String query, String siteUrl);
}
