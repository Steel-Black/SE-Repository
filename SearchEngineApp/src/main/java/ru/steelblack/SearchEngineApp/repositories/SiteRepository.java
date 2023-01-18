package ru.steelblack.SearchEngineApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.steelblack.SearchEngineApp.models.Site;
import ru.steelblack.SearchEngineApp.models.Status;

import java.util.List;

@Transactional
public interface SiteRepository extends JpaRepository<Site, Integer> {

    Site findByUrlAndAndStatus(String url, Status status);

    List<Site> findAllByStatus(Status status);


}
