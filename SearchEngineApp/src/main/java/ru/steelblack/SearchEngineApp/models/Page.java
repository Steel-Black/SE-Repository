package ru.steelblack.SearchEngineApp.models;

import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "page")
@Getter
@Setter
public class Page implements Comparable {

    @Id
//    @GenericGenerator(name = "page_id_generator", strategy = "sequence", parameters = {
//            @org.hibernate.annotations.Parameter(name = "sequence_name", value = "page_id_seq"),
//            @org.hibernate.annotations.Parameter(name = "allocated_size", value = "1"),
//            @org.hibernate.annotations.Parameter(name = "increment_size", value = "50"),
//            @org.hibernate.annotations.Parameter(name = "optimizer", value = "pooled-lo")
//    })
//    @SequenceGenerator(name = "page_seq", sequenceName = "page_sequence", initialValue = 1, allocationSize = 50)

    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "Id")
    private int id;

    @Column(name = "path", columnDefinition = "varchar(1000)", unique = true, nullable = false)
    private String path;

    @Column(name = "code", columnDefinition = "int", nullable = false)
    private int code;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String html;


    @Transient
    private List<Lemma> lemmas;


    @ManyToOne()
    @JoinColumn(name = "site_id")
    private Site site;

    @OneToMany(mappedBy = "page", cascade = CascadeType.REMOVE)
    @Cascade(org.hibernate.annotations.CascadeType.DELETE)
    private List<Index> indexList;

    public Page() {
    }

    public Page(String path, int code, String html) {
        this.path = path;
        this.code = code;
        this.html = html;
    }

    public List<Lemma> getLemmas(){
        if (lemmas == null){
            lemmas = new ArrayList<>();
        }
        for (Index index:indexList){
            lemmas.add(index.getLemma());
        }
        return lemmas;
    }

    public void addIndex(Index index){
        if (indexList == null){
            indexList = new ArrayList<>();
        }
        indexList.add(index);
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Page page = (Page) o;

        if (id != page.id) return false;
        if (code != page.code) return false;
        if (path != null ? !path.equals(page.path) : page.path != null) return false;
        return html != null ? html.equals(page.html) : page.html == null;
    }

    @Override
    public int hashCode() {
        int result = id;
        result = 31 * result + (path != null ? path.hashCode() : 0);
        result = 31 * result + code;
        result = 31 * result + (html != null ? html.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Page{" +
                "id=" + id +
                ", path='" + path + '\'' +
                ", code=" + code +
                '}';
    }

    @Override
    public int compareTo(Object o) {
        Page page = (Page) o;
        return this.getId()-page.getId();
    }

}
