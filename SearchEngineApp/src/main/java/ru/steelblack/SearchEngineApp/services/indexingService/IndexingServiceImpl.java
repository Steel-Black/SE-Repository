package ru.steelblack.SearchEngineApp.services.indexingService;

import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.steelblack.SearchEngineApp.dto.IndexingDTO.IndexingResponseError;
import ru.steelblack.SearchEngineApp.dto.IndexingDTO.IndexingResponseOk;
import ru.steelblack.SearchEngineApp.services.indexingService.indexingPage.PageParser;
import ru.steelblack.SearchEngineApp.models.*;
import ru.steelblack.SearchEngineApp.dao.hibernate.HibernateDAO;
import ru.steelblack.SearchEngineApp.dao.jdbcTemplate.JdbcTemplateDAO;
import ru.steelblack.SearchEngineApp.dto.IndexingDTO.IndexingResponse;
import ru.steelblack.SearchEngineApp.dto.statictics.DetailedStatisticsItem;
import ru.steelblack.SearchEngineApp.dto.statictics.StatisticsData;
import ru.steelblack.SearchEngineApp.dto.statictics.StatisticsResponse;
import ru.steelblack.SearchEngineApp.dto.statictics.TotalStatistics;
import ru.steelblack.SearchEngineApp.config.SitesList;
import ru.steelblack.SearchEngineApp.repositories.IndexRepository;
import ru.steelblack.SearchEngineApp.repositories.LemmaRepository;
import ru.steelblack.SearchEngineApp.repositories.PageRepository;
import ru.steelblack.SearchEngineApp.repositories.SiteRepository;

import java.util.*;
import java.util.concurrent.ForkJoinPool;


@Service
@Log4j2
public class IndexingServiceImpl implements StatisticService {

    private final LemmaRepository lemmaRepository;
    private final SiteRepository siteRepository;
    private final PageRepository pageRepository;
    private final JdbcTemplateDAO jdbcTemplateDAO;
    private final IndexRepository indexRepository;
    private final SitesList sites;
    private final HibernateDAO hibernateDAO;
    private boolean isStarted;

    @Autowired
    public IndexingServiceImpl(LemmaRepository lemmaRepository, SiteRepository siteRepository, PageRepository pageRepository, JdbcTemplateDAO jdbcTemplateDAO, IndexRepository indexRepository, SitesList sites, HibernateDAO hibernateDAO) {
        this.lemmaRepository = lemmaRepository;
        this.siteRepository = siteRepository;
        this.pageRepository = pageRepository;
        this.jdbcTemplateDAO = jdbcTemplateDAO;
        this.indexRepository = indexRepository;
        this.sites = sites;
        this.hibernateDAO = hibernateDAO;
    }

    public IndexingResponse getStartIndexingSites() {
        log.info("Start indexing All sites");
        List<Site> indexingSites = siteRepository.findAllByStatus(Status.INDEXING);

        if (indexingSites.isEmpty() && !isStarted) {
            isStarted = true;
            new Thread(this::indexedSites).start();

            return new IndexingResponseOk();
        } else {
            log.info("???????????????????? ?????? ????????????????");
            return new IndexingResponseError(false, "???????????????????? ?????? ????????????????");
        }
    }

    @Override
    public IndexingResponse getStartIndexingPage(String path) {
        Page page = pageRepository.findAllByPath(path);

        if (page == null) {
            log.info("C?????????????? " + path + " ?????????????????? ???? ?????????????????? ????????????, ?????????????????? ?? ???????????????????????????????? ??????????.");
            return new IndexingResponseError(false, "???????????? ???????????????? ?????????????????? ???? ?????????????????? ????????????, ?????????????????? ?? ???????????????????????????????? ??????????.");
        }
        log.info("Start indexing Page with path: " + path);
        indexedPage(page);

        return new IndexingResponseOk();
    }

    public void indexedSites() {

        List<Site> sitesList = new ArrayList<>(sites.getSites());

        jdbcTemplateDAO.deleteAllSites();

        List<List<Index>> indexingResults = getIndexingResult(sitesList);

        if (!PageParser.isTerminate()) {
            saveIndexingResults(sitesList, indexingResults);
            updateAllSites(sitesList, Status.INDEXED);
            isStarted = false;
            log.info("indexing sites complete");

        } else {
            log.info("???????????????????? ????????????????");
            updateAllSites(sitesList, "???????????????????? ????????????????", Status.FAILED);
            isStarted = false;
        }
    }

    public void indexedPage(Page page) {

        List<Lemma> lemmas = page.getLemmas();

        lemmas.forEach(lemma -> lemma.addFrequency(-1));

        jdbcTemplateDAO.deleteIndexesByPageId(page.getId());

        PageParser parser = new PageParser(page.getSite(), page.getPath());

        List<Index> indexList = parser.indexingPage(page);

        Page updatedPage = indexList.get(0).getPage();

        updatedPage.setId(page.getId());

        pageRepository.save(page);

        lemmaRepository.saveAll(updatedPage.getLemmas());

        jdbcTemplateDAO.indexBatchInsert(indexList);
        log.info("page indexing complete");
    }

    @Override
    public IndexingResponse terminate() {
        if (!isStarted) {
            log.info("???????????????????? ???? ????????????????");
            return new IndexingResponseError(false, "???????????????????? ???? ????????????????");
        }
        PageParser.terminate();
        return new IndexingResponseOk();
    }


    public void savePagesAndLemmas(Site site) {
        pageRepository.saveAll(site.getPageList());
        lemmaRepository.saveAll(site.getLemmasList());
    }

    public void saveIndexingResults(List<Site> sites, List<List<Index>> tasksResult) {
        for (Site site : sites) {
            savePagesAndLemmas(site);
        }
        for (List<Index> indexList : tasksResult) {
            jdbcTemplateDAO.indexBatchInsert(indexList);
        }
    }


    public void updateAllSites(List<Site> sitesList, Status status) {
        for (Site site : sitesList) {
            hibernateDAO.updateSite(site, status);
        }
    }

    public void updateAllSites(List<Site> sitesList, String lastError, Status status) {
        for (Site site : sitesList) {
            site.setLastError(lastError);
            hibernateDAO.updateSite(site, status);
        }
    }

    private List<List<Index>> getIndexingResult(List<Site> sitesList) {
        List<PageParser> tasks = new ArrayList<>();
        List<List<Index>> taskResultList = new ArrayList<>();

        for (Site site : sitesList) {
            ForkJoinPool fjp = new ForkJoinPool();
            PageParser task = new PageParser(site, site.getUrl());
            tasks.add(task);
            fjp.execute(task);
            hibernateDAO.saveSite(site, Status.INDEXING);
        }

        for (PageParser task : tasks) {
            taskResultList.add(task.join());
        }

        return taskResultList;
    }

    @Override
    public StatisticsResponse getStatistics() {
        List<Site> sites = siteRepository.findAll();

        TotalStatistics total = new TotalStatistics();
        total.setSites(sites.size());
        total.setIndexing(true);

        List<DetailedStatisticsItem> detailed = new ArrayList<>();

        for (Site site : sites) {
            DetailedStatisticsItem item = new DetailedStatisticsItem();
            item.setName(site.getName());
            item.setUrl(site.getUrl());
            int pages = site.getPageList().size();
            int lemmas = site.getLemmasList().size();
            item.setPages(pages);
            item.setLemmas(lemmas);
            item.setStatus(site.getStatus().toString());
            item.setError(site.getLastError());
            item.setStatusTime(site.getStatusTime());
            total.setPages(total.getPages() + pages);
            total.setLemmas(total.getLemmas() + lemmas);
            detailed.add(item);
        }

        StatisticsResponse response = new StatisticsResponse();
        StatisticsData data = new StatisticsData();
        data.setTotal(total);
        data.setDetailed(detailed);
        response.setStatistics(data);
        response.setResult(true);
        return response;
    }

}
