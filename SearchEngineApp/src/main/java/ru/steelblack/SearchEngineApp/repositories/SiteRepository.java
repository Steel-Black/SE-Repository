package ru.steelblack.SearchEngineApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.steelblack.SearchEngineApp.models.Site;
import ru.steelblack.SearchEngineApp.models.Status;

import java.util.List;
import java.util.Optional;


@Transactional
public interface SiteRepository extends JpaRepository<Site, Integer> {
//    Optional<List<Site>> findByStatus(Status status);

    Site findByUrlAndAndStatus(String url, Status status);

    List<Site> findAllByStatus(Status indexing);


}
