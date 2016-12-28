package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

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
    public void insert(List<ExamAccommodation> accommodations) {
        String SQL = "INSERT INTO exam_accommodation(exam_id, segment_key, type, code, description) \n" +
            "VALUES(:examId, :segmentKey, :type, :code, :description)";

        accommodations.forEach(examAccommodation -> {
            SqlParameterSource parameters = new MapSqlParameterSource("examId", UuidAdapter.getBytesFromUUID(examAccommodation.getExamId()))
                .addValue("segmentKey", examAccommodation.getSegmentKey())
                .addValue("type", examAccommodation.getType())
                .addValue("code", examAccommodation.getCode())
                .addValue("description", examAccommodation.getDescription());

            KeyHolder keyHolder = new GeneratedKeyHolder();

            jdbcTemplate.update(SQL, parameters, keyHolder);

            examAccommodation.setId(keyHolder.getKey().longValue());

            update(examAccommodation);
        });
    }

    @Override
    public void update(ExamAccommodation examAccommodation) {
        String SQL = "INSERT INTO exam_accommodation_event(exam_accommodation_id, denied_at, deleted_at) \n" +
            "VALUES(:examAccommodationId, :deniedAt, :deletedAt);";

        SqlParameterSource parameters = new MapSqlParameterSource("examAccommodationId", examAccommodation.getId())
            .addValue("deniedAt", mapJodaInstantToTimestamp(examAccommodation.getDeniedAt()))
            .addValue("deletedAt", mapJodaInstantToTimestamp(examAccommodation.getDeletedAt()));

        jdbcTemplate.update(SQL, parameters);
    }
}
