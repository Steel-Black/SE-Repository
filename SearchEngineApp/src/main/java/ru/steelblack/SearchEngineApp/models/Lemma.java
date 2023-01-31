package ru.steelblack.SearchEngineApp.models;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "lemma")
@Getter
@Setter
@NoArgsConstructor
public class Lemma implements Comparable {

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "frequency", nullable = false)
    private int frequency;

    @Transient
    private List<Page> pages;

    @ManyToOne
    @JoinColumn(name = "site_id", referencedColumnName = "id")
    private Site site;

    @OneToMany(mappedBy = "lemma")
    private List<Index> indexList;

    public Lemma(String name, int frequency) {
        this.name = name;
        this.frequency = frequency;
    }

    public List<Page> getPages(){
        if (this.pages == null) {
            this.pages = new ArrayList<>();
        }
        if (!pages.isEmpty()){
            return pages;
        }
        for (Index index:indexList){
            pages.add(index.getPage());
        }
        return pages;
    }

    public void addIndex(Index index){
        if (this.indexList == null)
        {
            this.indexList = new ArrayList<>();
        }
        indexList.add(index);
    }

    public void addSite(Site site){
        this.setSite(site);
        site.addLemma(this);
    }
    public void addFrequency (int frequency){
        setFrequency(this.getFrequency() + frequency);
    }

    @Override
    public int compareTo(Object o) {
        Lemma l = (Lemma) o;
        return this.getFrequency() - l.getFrequency();
    }

    @Override
    public String toString() {
        return "Lemma{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", frequency=" + frequency +
                ", site=" + site +
                '}';
    }

}
