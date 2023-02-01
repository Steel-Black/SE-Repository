package ru.steelblack.SearchEngineApp.services.searchService;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.steelblack.SearchEngineApp.dto.SearchDTO.ResponseDataError;
import ru.steelblack.SearchEngineApp.services.indexingService.indexingPage.LemmaFinder;
import ru.steelblack.SearchEngineApp.models.*;
import ru.steelblack.SearchEngineApp.dto.SearchDTO.ResponseData;
import ru.steelblack.SearchEngineApp.dto.SearchDTO.Data;
import ru.steelblack.SearchEngineApp.repositories.IndexRepository;
import ru.steelblack.SearchEngineApp.repositories.LemmaRepository;
import ru.steelblack.SearchEngineApp.repositories.PageRepository;
import ru.steelblack.SearchEngineApp.repositories.SiteRepository;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Log4j2
public class SearchServiceImpl implements SearchService {

    private final LemmaRepository lemmaRepositories;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;
    private static final int LIMIT_FREQUENCY = 50;


    @Autowired
    public SearchServiceImpl(LemmaRepository lemmaRepositories1, SiteRepository siteRepository, PageRepository pageRepository, IndexRepository indexRepository) {
        this.lemmaRepositories = lemmaRepositories1;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.indexRepository = indexRepository;
    }

    @Override
    public ResponseData searchPages(String query, String siteUrl) {
        log.info("Начало поиска: " + query);
        if (query == null || query.isBlank()) {
            log.info("Задан пустой поисковый запрос");
            return new ResponseDataError(false, "Задан пустой поисковый запрос");
        }

        List<Site> sites = getSites(siteUrl);

        if (sites.isEmpty()) {
            log.info("В данный момент нет сайтов доступных для поиска");
            return new ResponseDataError(false, "В данный момент нет сайтов доступных для поиска");
        }

        ResponseData responseData = new ResponseData();

        LemmaFinder lemmaFinder = LemmaFinder.getInstance();

        HashSet<String> lemmasNamesSet = lemmaFinder.getLemmaSet(query);

        for (Site site : sites) {
            List<Lemma> lemmas = lemmaRepositories.findLemmaBySiteIdAndNameIn(site.getId(), List.copyOf(lemmasNamesSet));

            if (lemmas.size() == lemmasNamesSet.size()) {
                List<Page> pages = getPagesWhereAllLemmasMeet(lemmas);
                if (pages.isEmpty()) {
                    continue;
                }
                HashMap<Page, Float> pageFloatHashMap = getPageWithRating(pages, lemmas);
                List<Data> searchData = getSearchData(pageFloatHashMap, lemmasNamesSet, site);
                responseData.setCount(responseData.getCount() + searchData.size());
                responseData.addToSearchDataList(searchData);
                responseData.setResult(true);
            }
        }
        if (responseData.getCount() == 0){
            return new ResponseDataError(false,"По вашему запросу ничего не найдено :(");
        }
        log.info("Найдено: " + responseData.getCount() + " результатов");
        return responseData;
    }

    private List<Site> getSites(String siteUrl) {

        List<Site> sites = new ArrayList<>();
        if (siteUrl != null) {
            Site optionalSite = siteRepository.findByUrlAndAndStatus(siteUrl, Status.INDEXED);
            sites.add(optionalSite);
        } else {
            List<Site> optionalSites = siteRepository.findAllByStatus(Status.INDEXED);
            sites.addAll(optionalSites);
        }
        return sites;
    }

    private List<Page> getPagesWhereAllLemmasMeet(List<Lemma> lemmas) {

        lemmas = lemmas.stream()
                .filter(lemma -> lemma.getFrequency() < LIMIT_FREQUENCY)
                .sorted()
                .collect(Collectors.toList());
        if (lemmas.isEmpty()) {
            return Collections.emptyList();
        }

        List<Page> pages = new CopyOnWriteArrayList<>(lemmas.get(0).getPages());

        lemmas.remove(lemmas.get(0));

        for (Page page : pages) {
            List<Lemma> pageLemmas = page.getLemmas();
            for (Lemma lemma : lemmas) {
                if (!pageLemmas.contains(lemma)) {
                    pages.remove(page);
                }
            }
        }
        return pages;
    }

    private String getTitle(Page page) {

        String html = page.getHtml();
        String substring = "<title>";
        String substring2 = "</title>";
        int start = html.indexOf(substring);
        int end = html.indexOf(substring2);
        return html.substring(start + substring.length(), end);
    }

    private String getSnippet(Page page, Set<String> lemmasSet) {

        String html = page.getHtml();
        StringBuilder stringBuilder = new StringBuilder();
        for (String lemma : lemmasSet) {
            stringBuilder.append(lemma).append("|");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() - 1);

        String regex = "([А-Я])([А-я\\s,\\-()0-9\"»«:]*)" + "(" + stringBuilder + ")" + "([А-я\\s,\\-()0-9\"»«:]*)\\.";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(html);

        if (matcher.find()) {
            String snippet = matcher.group();
            for (String lemma: lemmasSet) {
                snippet = snippet.replaceAll(lemma, "<b>" + lemma + "</b>");
            }
            return snippet;
        }
        return "";
    }

    private List<Data> getSearchData(HashMap<Page, Float> pageFloatHashMap, Set<String> lemmasSet, Site site) {

        float maxValue = pageFloatHashMap.values().stream().max(Float::compareTo).get();

        List<Data> data = new ArrayList<>();

        for (Page page : pageFloatHashMap.keySet()) {
            String title = getTitle(page);
            String snippet = getSnippet(page, lemmasSet);
            float relevance = pageFloatHashMap.get(page) / maxValue;
            String path = page.getPath().substring(site.getUrl().length());
            data.add(new Data(site.getUrl(), site.getName(), path, title, snippet, relevance));
        }

        return data;
    }

    private HashMap<Page, Float> getPageWithRating(List<Page> pages, List<Lemma> lemmas) {

        List<Index> indexes = indexRepository.findByLemmaInAndPageIn(lemmas, pages);

        HashMap<Page, Float> pageWithRating = new HashMap<>();

        for (Index index : indexes) {
            pageWithRating.put(index.getPage(), index.getRank());
            if (pageWithRating.containsKey(index.getPage())) {
                Page page = index.getPage();
                float rank = pageWithRating.get(page);
                pageWithRating.put(page, rank + index.getRank());
            }
        }
        return pageWithRating;
    }

}
