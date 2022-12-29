package ru.steelblack.SearchEngineApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.steelblack.SearchEngineApp.models.Index;
import ru.steelblack.SearchEngineApp.models.Lemma;
import ru.steelblack.SearchEngineApp.models.Page;

import java.util.List;

public interface IndexRepository extends JpaRepository<Index, Integer> {
    List<Index> findByLemmaInAndPageIn(List<Lemma> lemmas, List<Page> pages);
}
