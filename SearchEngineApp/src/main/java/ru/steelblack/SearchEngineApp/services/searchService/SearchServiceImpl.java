package ru.steelblack.SearchEngineApp.services.searchService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.steelblack.SearchEngineApp.services.IndexingService.indexingPage.LemmaFinder;
import ru.steelblack.SearchEngineApp.models.*;
import ru.steelblack.SearchEngineApp.pageDTO.SearchDTO.ResponseData;
import ru.steelblack.SearchEngineApp.pageDTO.SearchDTO.Data;
import ru.steelblack.SearchEngineApp.repositories.IndexRepository;
import ru.steelblack.SearchEngineApp.repositories.LemmaRepository;
import ru.steelblack.SearchEngineApp.repositories.PageRepository;
import ru.steelblack.SearchEngineApp.repositories.SiteRepository;
import ru.steelblack.SearchEngineApp.util.SearchEngineException.BadRequestException;
import ru.steelblack.SearchEngineApp.util.SearchEngineException.SiteNotFoundException;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class SearchServiceImpl implements SearchService {

    private final LemmaRepository lemmaRepositories;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final IndexRepository indexRepository;


    @Autowired
    public SearchServiceImpl(LemmaRepository lemmaRepositories1, SiteRepository siteRepository, PageRepository pageRepository, IndexRepository indexRepository) {
        this.lemmaRepositories = lemmaRepositories1;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.indexRepository = indexRepository;
    }
    @Override
    public ResponseData searchPages(String query, String siteUrl) {

        if (query == null || query.isBlank()){
            throw new BadRequestException("Задан пустой поисковый запрос");
        }

        List<Site> sites = getSites(siteUrl);

        ResponseData responseData = new ResponseData();

        LemmaFinder lemmaFinder = LemmaFinder.getInstance();

        HashSet<String> lemmasNamesSet = lemmaFinder.getLemmaSet(query);

        for (Site site:sites){
            List<Lemma> lemmas = lemmaRepositories.findLemmaBySiteIdAndNameIn(site.getId(), List.copyOf(lemmasNamesSet));
            if (lemmas.size() == lemmasNamesSet.size()) {
                List<Page> pages = getPagesWhereAllLemmasMeet(lemmas);
                if (pages.isEmpty()){
                    continue;
                }
                HashMap<Page,Float> pageFloatHashMap = getPageWithRating(pages, lemmas);
                List<Data> searchData = getSearchData(pageFloatHashMap, lemmasNamesSet, site);
                responseData.setCount(responseData.getCount() + searchData.size());
                responseData.addToSearchDataList(searchData);
                responseData.setResult(true);
            }
        }

        return responseData;
    }

    private List<Site> getSites(String siteUrl){
        List<Site> sites = new ArrayList<>();
        if (siteUrl != null){
            Optional<Site> optionalSite = siteRepository.findByUrlAndAndStatus(siteUrl, Status.INDEXED);
            if (optionalSite.isPresent()){
                sites.add(optionalSite.get());
            }
            else {
                throw new SiteNotFoundException("В данный момент сайт не дуступен для поиска");
            }

        }
        else{
            Optional<List<Site>> optionalSites = siteRepository.findByStatus(Status.INDEXED);
            if (optionalSites.isPresent()){
                sites.addAll(optionalSites.get());
            }
            else {
                throw new SiteNotFoundException("В данный момент сайты не дуступены для поиска");
            }
        }
        return sites;
    }

    private List<Page> getPagesWhereAllLemmasMeet(List<Lemma> lemmas) {
        int limitFrequency = 30;
        lemmas = lemmas.stream()
                .filter(lemma -> lemma.getFrequency() < limitFrequency)
                .sorted()
                .collect(Collectors.toList());
        if (lemmas.isEmpty()){
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

    private String getTitle(Page page){

        String html = page.getHtml();
        String substring = "<title>";
        String substring2 = "</title>";
        int start = html.indexOf(substring);
        int end = html.indexOf(substring2);
        return html.substring(start + substring.length(), end);
    }

    private String getSnippet(Page page, Set<String> lemmasSet){

        String html = page.getHtml();
        StringBuilder stringBuilder = new StringBuilder();
        for (String lemma: lemmasSet){
            stringBuilder.append("(").append(lemma).append(")|");
        }
        stringBuilder.deleteCharAt(stringBuilder.length() -1);

        String regex = "([А-Я])([А-я\\s,\\-]*)" + stringBuilder + "([А-я\\s,\\-]*)\\.";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(html);

        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    private List<Data> getSearchData(HashMap<Page,Float> pageFloatHashMap, Set<String> lemmasSet, Site site){

        float maxValue = pageFloatHashMap.values().stream().max(Float::compareTo).get();

        List<Data> data = new ArrayList<>();

        for (Page page:pageFloatHashMap.keySet()){
            String title = getTitle(page);
            String snippet = getSnippet(page,lemmasSet);
            float relevance = pageFloatHashMap.get(page) / maxValue;
            data.add(new Data(site.getUrl(), site.getName(), page.getPath(), title, snippet, relevance));
        }

        return data;
    }

    private HashMap<Page,Float> getPageWithRating(List<Page> pages, List<Lemma> lemmas){

        List<Index> indexes = indexRepository.findByLemmaInAndPageIn(lemmas, pages);

        HashMap<Page, Float> pageWithRating = new HashMap<>();

        for (Index index:indexes){
            pageWithRating.put(index.getPage(), index.getRank());
            if (pageWithRating.containsKey(index.getPage())){
                Page page = index.getPage();
                float rank = pageWithRating.get(page);
                pageWithRating.put(page, rank + index.getRank());
            }
        }
        return pageWithRating;
    }

}
