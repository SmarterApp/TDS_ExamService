package tds.exam.repository.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;

import tds.exam.Exam;
import tds.exam.repository.ExamQueryRepository;

import java.util.UUID;

@Repository
public class ExamQueryRepositoryImpl implements ExamQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamQueryRepositoryImpl(@Qualifier("queryDataSource") DataSource queryDataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(queryDataSource);
    }

    @Override
    public Exam getExamById(UUID id) {
        return new Exam(id);
    }
}
