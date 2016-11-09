package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tds.common.data.mysql.UuidAdapter;
import tds.exam.ExamAccommodation;
import tds.exam.repositories.AccommodationCommandRepository;

@Repository
public class AccommodationCommandRepositoryImpl implements AccommodationCommandRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public AccommodationCommandRepositoryImpl(@Qualifier("commandJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insertAccommodations(List<ExamAccommodation> accommodations) {
        String SQL = "INSERT INTO exam_accommodations(exam_id, segment_id, type, code, description, denied_at)" +
            "VALUES(:examId, :segmentId, :type, :code, :description, :deniedAt)";

        List<SqlParameterSource> parameterSources = new ArrayList<>();
        accommodations.forEach(examAccommodation -> {
            SqlParameterSource parameters = new MapSqlParameterSource("examId", UuidAdapter.getBytesFromUUID(examAccommodation.getExamId()))
                .addValue("segmentId", examAccommodation.getSegmentId())
                .addValue("type", examAccommodation.getType())
                .addValue("code", examAccommodation.getCode())
                .addValue("description", examAccommodation.getDescription())
                .addValue("deniedAt", examAccommodation.getDeniedAt() == null ? null : Date.from(examAccommodation.getDeniedAt()));

            parameterSources.add(parameters);
        });

        jdbcTemplate.batchUpdate(SQL, parameterSources.toArray(new SqlParameterSource[parameterSources.size()]));
    }
}
