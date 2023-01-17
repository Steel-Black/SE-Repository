package ru.steelblack.SearchEngineApp.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;
import ru.steelblack.SearchEngineApp.models.Page;



@Transactional
public interface PageRepository extends JpaRepository<Page, Integer> {
    Page findAllByPath(String path);
}
