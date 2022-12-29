package ru.steelblack.SearchEngineApp.packageDAO.jdbcTemplate;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import ru.steelblack.SearchEngineApp.models.*;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class JdbcTemplateDAO {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public JdbcTemplateDAO(JdbcTemplate jdbcTemplate){
        this.jdbcTemplate = jdbcTemplate;
    }

    public void indexBatchInsert(List<Index> indexList) {

        jdbcTemplate.batchUpdate("INSERT INTO index (page_id, lemma_id, rank) VALUES (?,?,?)", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Index index = indexList.get(i);

                ps.setInt(1, index.getPage().getId());
                ps.setInt(2, index.getLemma().getId());
                ps.setFloat(3, index.getRank());
            }
            @Override
            public int getBatchSize() {
                return indexList.size();
            }
        }); }

    public void deleteAllSites(){
        jdbcTemplate.update("delete from Index");
        jdbcTemplate.update("delete from Page");
        jdbcTemplate.update("delete from Lemma");
        jdbcTemplate.update("delete from Site");
    }

    public void deleteOneSite(Site site){

        List<Lemma> lemmas = site.getLemmasList();
        List<Page>  pages = site.getPageList();
        jdbcTemplate.batchUpdate("delete from Index where page_id=?", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                preparedStatement.setInt(1, pages.get(i).getId());
            }
            @Override
            public int getBatchSize() {
                return pages.size();
            }
        });
        jdbcTemplate.batchUpdate("delete from Page where id=?", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                preparedStatement.setInt(1, pages.get(i).getId());
            }

            @Override
            public int getBatchSize() {
                return pages.size();
            }
        });
        jdbcTemplate.batchUpdate("delete from Lemma where id=?", new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                preparedStatement.setInt(1, lemmas.get(i).getId());
            }
            @Override
            public int getBatchSize() {
                return lemmas.size();
            }
        });
        jdbcTemplate.update("delete from Site where id =?", site.getId());

    }

    public void deleteIndexesByPageId(int id){
        jdbcTemplate.update("delete from Index where page_id=?", id);
    }





}
