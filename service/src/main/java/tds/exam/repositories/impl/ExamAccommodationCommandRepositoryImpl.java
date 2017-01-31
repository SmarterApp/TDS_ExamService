package tds.exam.repositories.impl;

import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

import tds.exam.ExamAccommodation;
import tds.exam.repositories.ExamAccommodationCommandRepository;

import static tds.common.data.mapping.ResultSetMapperUtility.mapJodaInstantToTimestamp;

@Repository
public class ExamAccommodationCommandRepositoryImpl implements ExamAccommodationCommandRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamAccommodationCommandRepositoryImpl(@Qualifier("commandJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(List<ExamAccommodation> accommodations) {
        String SQL = "INSERT INTO exam_accommodation(exam_id, id, segment_key, type, code, description, allow_change, value, segment_position) \n" +
            "VALUES(:examId, :id, :segmentKey, :type, :code, :description, :allowChange, :value, :segmentPosition)";

        SqlParameterSource[] parameters = accommodations.stream().map(examAccommodation ->
            new MapSqlParameterSource("examId", examAccommodation.getExamId().toString())
                .addValue("id", examAccommodation.getId().toString())
                .addValue("segmentKey", examAccommodation.getSegmentKey())
                .addValue("type", examAccommodation.getType())
                .addValue("code", examAccommodation.getCode())
                .addValue("allowChange", examAccommodation.isAllowChange())
                .addValue("value", examAccommodation.getValue())
                .addValue("segmentPosition", examAccommodation.getSegmentPosition())
                .addValue("description", examAccommodation.getDescription())).toArray(SqlParameterSource[]::new);


        jdbcTemplate.batchUpdate(SQL, parameters);

        update(accommodations.toArray(new ExamAccommodation[accommodations.size()]));
    }

    @Override
    public void update(ExamAccommodation... examAccommodation) {
        updateEvent(examAccommodation);
    }

    private void updateEvent(ExamAccommodation... examAccommodations) {
        String SQL = "INSERT INTO exam_accommodation_event(" +
            "exam_accommodation_id, " +
            "denied_at, " +
            "deleted_at, " +
            "selectable," +
            "total_type_count) \n" +
            "VALUES(" +
            ":examAccommodationId, " +
            ":deniedAt, " +
            ":deletedAt, " +
            ":selectable," +
            ":totalTypeCount);";

        SqlParameterSource[] parameterSources = new SqlParameterSource[examAccommodations.length];

        for (int i = 0; i < parameterSources.length; i++) {
            ExamAccommodation examAccommodation = examAccommodations[i];
            SqlParameterSource parameters = new MapSqlParameterSource("examAccommodationId", examAccommodation.getId().toString())
                .addValue("deniedAt", mapJodaInstantToTimestamp(examAccommodation.getDeniedAt()))
                .addValue("selectable", examAccommodation.isSelectable())
                .addValue("totalTypeCount", examAccommodation.getTotalTypeCount())
                .addValue("deletedAt", mapJodaInstantToTimestamp(examAccommodation.getDeletedAt()));

            parameterSources[i] = parameters;
        }

        jdbcTemplate.batchUpdate(SQL, parameterSources);
    }

    @Override
    public void delete(List<ExamAccommodation> accommodations) {
        Instant deletedAt = Instant.now();

        List<ExamAccommodation> accommodationsToDelete = accommodations.stream()
            .map(accommodation -> new ExamAccommodation
                .Builder(accommodation.getId())
                .fromExamAccommodation(accommodation)
                .withDeletedAt(deletedAt)
                .build())
            .collect(Collectors.toList());

        updateEvent(accommodationsToDelete.toArray(new ExamAccommodation[accommodationsToDelete.size()]));
    }
}
