package ru.steelblack.SearchEngineApp.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;


import javax.sql.DataSource;
import java.util.Properties;

@Configuration
@ComponentScan("ru.steelblack.SearchEngineApp")
@EnableTransactionManagement
@EnableWebMvc
@EnableJpaRepositories("ru.steelblack.SearchEngineApp.repositories")
public class SpringConfig {

    private final DataBaseProperties dataBaseProperties;

    @Autowired
    public SpringConfig(DataBaseProperties properties) {

        this.dataBaseProperties = properties;
    }

    @Bean
    public DataSource dataSource(){
        DriverManagerDataSource dataSource = new DriverManagerDataSource();

        dataSource.setUrl(dataBaseProperties.getDataSource().get("url"));
        dataSource.setUsername(dataBaseProperties.getDataSource().get("username"));
        dataSource.setPassword(dataBaseProperties.getDataSource().get("password"));
        return dataSource;
    }
    private Properties hibernateProperties(){
        Properties properties = new Properties();
        properties.put("hibernate.dialect", dataBaseProperties.getDataSource().get("dialect"));
        properties.put("hibernate.hbm2ddl.auto", dataBaseProperties.getDataSource().get("ddl-auto"));
        return properties;
    }

    @Bean
    public JdbcTemplate jdbcTemplate(){
        return new JdbcTemplate(dataSource());
    }

    @Bean
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(){
        final LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(dataSource());
        em.setPackagesToScan("ru.steelblack.SearchEngineApp.models");
        em.setJpaProperties(hibernateProperties());

        final HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);

        return em;
    }
    @Bean
    public PlatformTransactionManager transactionManager(){
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory().getObject());
        return transactionManager;
    }

}

