package ru.steelblack.SearchEngineApp.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.steelblack.SearchEngineApp.pageDTO.IndexingDTO.IndexingResponse;
import ru.steelblack.SearchEngineApp.pageDTO.SearchDTO.ResponseData;
import ru.steelblack.SearchEngineApp.pageDTO.statictics.StatisticsResponse;
import ru.steelblack.SearchEngineApp.services.searchService.SearchService;
import ru.steelblack.SearchEngineApp.services.IndexingService.StatisticService;
import ru.steelblack.SearchEngineApp.util.ExceptionResponse;
import ru.steelblack.SearchEngineApp.util.SearchEngineException.*;

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

        System.out.println("startIndexingAllSites");

        return new ResponseEntity<>(statisticsService.getStartIndexing(), HttpStatus.OK);

    }

    @GetMapping("/stopIndexing")
    public  ResponseEntity<IndexingResponse> stopIndexing() {
        System.out.println("stopIndexing");
        return new ResponseEntity<>(statisticsService.terminate(), HttpStatus.OK);
    }



    @PostMapping("/indexPage")
    public ResponseEntity<IndexingResponse> indexPage(@RequestParam String url){
        System.out.println("startIndexingOnePage");
         return  new ResponseEntity<>(statisticsService.indexingPage(url), HttpStatus.OK);
    }

    @GetMapping("/statistics")
    public ResponseEntity<StatisticsResponse> statistics() {
        return ResponseEntity.ok(statisticsService.getStatistics());
    }

    @ExceptionHandler
    private ResponseEntity<ExceptionResponse> myException(SearchEngineException e){

        ExceptionResponse response = new ExceptionResponse();
        response.setError(e.getMessage());
        ResponseEntity<ExceptionResponse> responseEntity = null;
        if (e.getClass() == PageNotFoundException.class){
            responseEntity = new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
        }
        if (e.getClass() == RepeatedRequestException.class){
            responseEntity = new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
        }
        if (e.getClass() == TerminateException.class){
            responseEntity = new ResponseEntity<>(response, HttpStatus.LOCKED);
        }
        if (e.getClass() == BadRequestException.class){
            responseEntity = new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
        }
        if (e.getClass() == SiteNotFoundException.class){
            responseEntity = new ResponseEntity<>(response, HttpStatus.METHOD_NOT_ALLOWED);
        }
        return responseEntity;
    }

}
