package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;

import tds.common.data.mysql.UuidAdapter;
import tds.exam.ExamAccommodation;
import tds.exam.repositories.ExamAccommodationCommandRepository;

import static tds.common.data.mapping.ResultSetMapperUtility.mapJodaInstantToTimestamp;

@Repository
public class ExamAccommodationCommandRepositoryImpl implements ExamAccommodationCommandRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ExamAccommodationCommandRepositoryImpl(@Qualifier("commandJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insertAccommodations(List<ExamAccommodation> accommodations) {
        String SQL = "INSERT INTO exam_accommodations(exam_id, segment_key, type, code, description, denied_at)" +
            "VALUES(:examId, :segmentKey, :type, :code, :description, :deniedAt)";

        List<SqlParameterSource> parameterSources = new ArrayList<>();
        accommodations.forEach(examAccommodation -> {
            SqlParameterSource parameters = new MapSqlParameterSource("examId", UuidAdapter.getBytesFromUUID(examAccommodation.getExamId()))
                .addValue("segmentKey", examAccommodation.getSegmentKey())
                .addValue("type", examAccommodation.getType())
                .addValue("code", examAccommodation.getCode())
                .addValue("description", examAccommodation.getDescription())
                .addValue("deniedAt", mapJodaInstantToTimestamp(examAccommodation.getDeniedAt()));

            parameterSources.add(parameters);
        });

        jdbcTemplate.batchUpdate(SQL, parameterSources.toArray(new SqlParameterSource[parameterSources.size()]));
    }
}
