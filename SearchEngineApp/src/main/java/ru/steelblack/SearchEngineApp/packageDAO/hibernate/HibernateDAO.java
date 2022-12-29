package ru.steelblack.SearchEngineApp.packageDAO.hibernate;


import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.steelblack.SearchEngineApp.models.*;

import javax.persistence.EntityManager;
import java.util.Date;
import java.util.List;

@Component
@Transactional
public class HibernateDAO {
    private final EntityManager entityManager;

    @Autowired
    public HibernateDAO(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public void saveSite(Site site, Status status){
        Session session = entityManager.unwrap(Session.class);
        site.setStatus(status);
        site.setStatusTime(new Date());
        session.save(site);
        session.close();
    }
}
