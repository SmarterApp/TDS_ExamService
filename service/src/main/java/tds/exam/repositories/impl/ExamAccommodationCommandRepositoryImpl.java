package tds.exam.repositories.impl;

import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.exam.ExamAccommodation;
import tds.exam.repositories.ExamAccommodationCommandRepository;

import static tds.common.data.mapping.ResultSetMapperUtility.mapJodaInstantToTimestamp;

@Repository
public class ExamAccommodationCommandRepositoryImpl implements ExamAccommodationCommandRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamAccommodationCommandRepositoryImpl(@Qualifier("commandJdbcTemplate") final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(final List<ExamAccommodation> accommodations) {
        String SQL = "INSERT INTO exam_accommodation(exam_id, id, segment_key, type, code, description, allow_change, value, segment_position, created_at) \n" +
            "VALUES(:examId, :id, :segmentKey, :type, :code, :description, :allowChange, :value, :segmentPosition, :createdAt)";

        Instant createdAt = Instant.now();
        List<ExamAccommodation> createdAccommodations = new ArrayList<>();
        SqlParameterSource[] parameters = accommodations.stream()
            .map(examAccommodation -> {
                createdAccommodations.add(ExamAccommodation.Builder
                    .fromExamAccommodation(examAccommodation)
                    .withCreatedAt(createdAt)
                    .build());

                return new MapSqlParameterSource("examId", examAccommodation.getExamId().toString())
                    .addValue("id", examAccommodation.getId().toString())
                    .addValue("segmentKey", examAccommodation.getSegmentKey())
                    .addValue("type", examAccommodation.getType())
                    .addValue("code", examAccommodation.getCode())
                    .addValue("allowChange", examAccommodation.isAllowChange())
                    .addValue("value", examAccommodation.getValue())
                    .addValue("segmentPosition", examAccommodation.getSegmentPosition())
                    .addValue("description", examAccommodation.getDescription())
                    .addValue("createdAt", mapJodaInstantToTimestamp(createdAt));
            })
            .toArray(SqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(SQL, parameters);

        update(createdAccommodations.toArray(new ExamAccommodation[createdAccommodations.size()]));
    }

    @Override
    public void update(final ExamAccommodation... examAccommodation) {
        updateEvent(examAccommodation);
    }

    private void updateEvent(final ExamAccommodation... examAccommodations) {
        String SQL = "INSERT INTO exam_accommodation_event(" +
            "exam_accommodation_id, " +
            "denied_at, " +
            "deleted_at, " +
            "selectable," +
            "custom," +
            "total_type_count," +
            "created_at) \n" +
            "VALUES(" +
            ":examAccommodationId, " +
            ":deniedAt, " +
            ":deletedAt, " +
            ":selectable," +
            ":custom," +
            ":totalTypeCount," +
            ":createdAt);";

        SqlParameterSource[] parameterSources = new SqlParameterSource[examAccommodations.length];

        for (int i = 0; i < parameterSources.length; i++) {
            ExamAccommodation examAccommodation = examAccommodations[i];
            SqlParameterSource parameters = new MapSqlParameterSource("examAccommodationId", examAccommodation.getId().toString())
                .addValue("deniedAt", mapJodaInstantToTimestamp(examAccommodation.getDeniedAt()))
                .addValue("selectable", examAccommodation.isSelectable())
                .addValue("totalTypeCount", examAccommodation.getTotalTypeCount())
                .addValue("custom", examAccommodation.isCustom())
                .addValue("deletedAt", mapJodaInstantToTimestamp(examAccommodation.getDeletedAt()))
                .addValue("createdAt", mapJodaInstantToTimestamp(Instant.now()));

            parameterSources[i] = parameters;
        }

        jdbcTemplate.batchUpdate(SQL, parameterSources);
    }

    @Override
    public void delete(final List<ExamAccommodation> accommodations) {
        Instant deletedAt = Instant.now();

        List<ExamAccommodation> accommodationsToDelete = accommodations.stream()
            .map(accommodation -> ExamAccommodation.Builder
                .fromExamAccommodation(accommodation)
                .withDeletedAt(deletedAt)
                .build())
            .collect(Collectors.toList());

        updateEvent(accommodationsToDelete.toArray(new ExamAccommodation[accommodationsToDelete.size()]));
    }
}
