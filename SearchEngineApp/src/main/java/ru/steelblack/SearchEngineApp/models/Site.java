package ru.steelblack.SearchEngineApp.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "site")
@Getter
@Setter
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private int id;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "status_time", nullable = false)
    @Temporal(value = TemporalType.TIMESTAMP)
     private Date statusTime;

    @Column(name = "last_error")
    private String lastError;

    @Column(name = "url", nullable = false)
    private String url;

    @Column(name = "name", nullable = false)
    private String name;

    @OneToMany(mappedBy = "site", cascade = CascadeType.REMOVE)
    private List<Page> pageList = new ArrayList<>();

    @OneToMany(mappedBy = "site", cascade = CascadeType.REMOVE)
    private List<Lemma> lemmasList = new ArrayList<>();

    public Site() {
    }

    public Site(Status status, Date statusTime, String lastError, String url, String name) {
        this.status = status;
        this.statusTime = statusTime;
        this.lastError = lastError;
        this.url = url;
        this.name = name;
    }

    public void addPage(Page page){
        page.setSite(this);
        pageList.add(page);
    }

    public void addLemma(Lemma lemma) {
        lemma.setSite(this);
        lemmasList.add(lemma);
    }
}