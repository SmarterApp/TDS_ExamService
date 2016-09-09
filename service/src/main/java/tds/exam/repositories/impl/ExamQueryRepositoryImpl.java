package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

import tds.common.data.mysql.UuidAdapter;
import tds.common.data.mysql.spring.UuidBeanPropertyRowMapper;
import tds.exam.Exam;
import tds.exam.repositories.ExamQueryRepository;

import java.util.Optional;
import java.util.UUID;

@Repository
public class ExamQueryRepositoryImpl implements ExamQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamQueryRepositoryImpl(@Qualifier("queryDataSource") DataSource queryDataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(queryDataSource);
    }

    @Override
    public Optional<Exam> getExamByUniqueKey(UUID uniqueKey) {
        final SqlParameterSource parameters = new MapSqlParameterSource("uniqueKey", UuidAdapter.getBytesFromUUID(uniqueKey));

        String query = "SELECT unique_key \n" +
            "FROM exam.exam \n" +
            "WHERE unique_key = :uniqueKey";

        Optional<Exam> examOptional;
        try {
            examOptional = Optional.of(jdbcTemplate.queryForObject(query, parameters, new UuidBeanPropertyRowMapper<>(Exam.class)));
        } catch (EmptyResultDataAccessException e) {
            examOptional = Optional.empty();
        }


        return examOptional;
    }
}
