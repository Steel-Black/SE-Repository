package ru.steelblack.SearchEngineApp.services.IndexingService.indexingPage;

import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import ru.steelblack.SearchEngineApp.models.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.RecursiveTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class PageParser extends RecursiveTask <List<Index>> {

    private static Set<String> urlSet = new HashSet<>();
    private List<Index> indexList = new ArrayList<>();
    private static boolean isTerminate;
    private Site site;
    private String url;

    public PageParser(Site site, String url) {
        this.site = site;
        this.url = url;
    }

    @Override
    protected List<Index> compute() {

        if (url.endsWith(".ru/") || url.endsWith(".com/")) {
            site.getPageList().clear();
            site.getLemmasList().clear();
            urlSet.clear();
            isTerminate = false;
        }

        if (!isTerminate) {
//            site.setStatus(Status.INDEXING);
//            site.setLastError(null);
//            site.setStatusTime(new Date());

            Connection connection = getConnection(url);

            Document doc = getDocument(connection);

            Page page = getPage(doc, connection);

            if (page.getCode() < 400) {
                List<Lemma> lemmaList = site.getLemmasList();
                indexList.addAll(getIndexes(doc, page, lemmaList));
            }
            List<PageParser> tasks = getNewTasksFromPage(doc);

            for (PageParser parser : tasks) {
                indexList.addAll(parser.join());
            }
        }
//        else {
//            site.setStatus(Status.FAILED);
//            site.setLastError("Остановка индексации");
//            site.setStatusTime(new Date());
//        }

        return indexList;
    }


    private Document getDocument(Connection connection) {
        Document document = null;
        try {
            document = connection.get();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return document;
    }

    private List<Index> getIndexes(Document doc, Page page, List<Lemma> lemmaList) {
        LemmaFinder lemmaFinder = LemmaFinder.getInstance();
        String titleWords = doc.head().text();
        String bodyWords = doc.body().text();

        HashMap<String, Integer> titleLemmas = lemmaFinder.collectLemmas(titleWords);
        HashMap<String, Integer> bodyLemmas = lemmaFinder.collectLemmas(bodyWords);
        HashSet<String> set = lemmaFinder.getLemmaSet(titleWords.concat(" ").concat(bodyWords));

        float rating = 0f;
        List<Index> indexes = new ArrayList<>();
        for (String name : set) {
            if (titleLemmas.containsKey(name) && bodyLemmas.containsKey(name)) {
                rating = titleLemmas.get(name) + bodyLemmas.get(name) * 0.8f;

            } else if (!titleLemmas.containsKey(name) && bodyLemmas.containsKey(name)) {
                rating = bodyLemmas.get(name) * 0.8f;

            } else if (!bodyLemmas.containsKey(name) && titleLemmas.containsKey(name)) {
                rating = titleLemmas.get(name) * 1f;
            }
            synchronized (site) {
                Lemma lemma = createOrGetLemma(name, lemmaList);

                Index index = new Index(page, lemma, rating);
                lemma.addIndex(index);
                page.addIndex(index);
                indexes.add(index);
            }
        }
        return indexes;
    }

    private Lemma createOrGetLemma(String name, List<Lemma> lemmaList) {

        for (Lemma lemma1 : lemmaList) {
            if (lemma1.getName().equals(name)) {
                lemma1.addFrequency(1);
                return lemma1;
            }
        }
        Lemma lemma = new Lemma(name, 1);
        lemma.addSite(site);

        return lemma;
    }

    private String getNewUrl(String link) {
        String regex = url + "([\\w-/=%[^?.#]]+)";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(link);
        if (matcher.find()) {
            return matcher.group();
        }
        return null;
    }

    private List<PageParser> getNewTasksFromPage(Document doc){
        List<PageParser> tasks = new ArrayList<>();
        Elements links = doc.select("a");
        for (Element element : links) {
            if (isTerminate){
                break;
            }
            String link = element.attr("abs:href");
            String newUrl = getNewUrl(link);
            if (newUrl != null && !urlSet.contains(newUrl)) {
                urlSet.add(newUrl);

                PageParser task = new PageParser(site, newUrl);
                tasks.add(task);
                task.fork();
            }
        }
        return tasks;
    }

    private Page getPage(Document doc, Connection connection){

        String html = doc.html();
        int code = connection.response().statusCode();
        urlSet.add(url);
//        String[] split = url.split("/",4);
//        String path = split[3];
        Page page = new Page(url, code, html);
        site.addPage(page);
        return page;
    }

    public List<Index> indexingPage(Page page) {

        Connection connection = getConnection(page.getPath());

        Document doc = getDocument(connection);
        Page updatedPage = getPage(doc, connection);

        return getIndexes(doc, updatedPage, page.getLemmas());

    }

    private Connection getConnection(String url){

        Connection connection = Jsoup.connect(url).userAgent("Mozilla/5.0 (Windows; U; WindowsNT 5.1; en-US; rv1.8.1.6) Gecko/20070725 Firefox/2.0.0.6").referrer("http://www.google.com").ignoreHttpErrors(true);

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return connection;
    }


    public static void Terminate() {
        isTerminate = true;
    }

    public static boolean isTerminate() {
        return isTerminate;
    }
}
