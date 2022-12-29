package ru.steelblack.SearchEngineApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.steelblack.SearchEngineApp.models.Lemma;

import javax.persistence.criteria.CriteriaBuilder;
import java.util.HashSet;
import java.util.List;
@Transactional
public interface LemmaRepository extends JpaRepository<Lemma, Integer> {
    List<Lemma> findLemmaBySiteIdAndNameIn(int id, List<String> namesList);
}
