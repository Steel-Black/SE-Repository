package ru.steelblack.SearchEngineApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.steelblack.SearchEngineApp.pageDTO.IndexingDTO.IndexingResponse;
import ru.steelblack.SearchEngineApp.pageDTO.SearchDTO.ResponseData;
import ru.steelblack.SearchEngineApp.pageDTO.statictics.StatisticsResponse;
import ru.steelblack.SearchEngineApp.services.searchService.SearchService;
import ru.steelblack.SearchEngineApp.services.indexingService.StatisticService;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final StatisticService statisticsService;
    private final SearchService searchServiceImpl;

    @Autowired
    public ApiController(StatisticService statisticsService, SearchService searchServiceImpl) {

        this.statisticsService =  statisticsService;
        this.searchServiceImpl = searchServiceImpl;
    }

    @GetMapping("/search")
    public ResponseData search(@RequestParam String query, @RequestParam(required = false) String site){
        return searchServiceImpl.searchPages(query, site);
    }


    @GetMapping("/startIndexing")
    public  ResponseEntity<IndexingResponse> startIndexing() {

        return new ResponseEntity<>(statisticsService.getStartIndexingSites(), HttpStatus.OK);

    }

    @GetMapping("/stopIndexing")
    public  ResponseEntity<IndexingResponse> stopIndexing() {

        return new ResponseEntity<>(statisticsService.terminate(), HttpStatus.OK);
    }



    @PostMapping("/indexPage")
    public ResponseEntity<IndexingResponse> indexPage(@RequestParam String url){

        return  new ResponseEntity<>(statisticsService.getStartIndexingPage(url), HttpStatus.OK);
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

}
