package ru.steelblack.SearchEngineApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.steelblack.SearchEngineApp.models.Page;
import java.util.Optional;


@Transactional
public interface PageRepository extends JpaRepository<Page, Integer> {
    Optional<Page> findPageByPath(String path);
}
