package ru.steelblack.SearchEngineApp.services.searchService;

import ru.steelblack.SearchEngineApp.dto.SearchDTO.ResponseData;

public interface SearchService {

    ResponseData searchPages(String query, String siteUrl);
}
