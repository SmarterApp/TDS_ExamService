package tds.exam.repositories.impl;

import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import tds.exam.ExamAccommodation;
import tds.exam.repositories.ExamAccommodationCommandRepository;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
        final String SQL =
            "INSERT INTO " +
                "   exam_accommodation(exam_id, id, segment_key, type, code, description, allow_change, value, segment_position, " +
                "   created_at, visible, student_controlled, disabled_on_guest_session, default_accommodation, allow_combine, sort_order, depends_on, functional) \n" +
                "VALUES(:examId, :id, :segmentKey, :type, :code, :description, :allowChange, :value, :segmentPosition, :createdAt," +
                "   :visible, :studentControlled, :disabledOnGuestSession, :defaultAccommodation, :allowCombine, :sortOrder, :dependsOn, :functional)";

        final Timestamp createdAt = mapJodaInstantToTimestamp(Instant.now());
        final List<ExamAccommodation> createdAccommodations = new ArrayList<>();
        final SqlParameterSource[] parameters = accommodations.stream()
            .map(examAccommodation -> {
                createdAccommodations.add(ExamAccommodation.Builder
                    .fromExamAccommodation(examAccommodation)
                    .build());

                return new MapSqlParameterSource("examId", examAccommodation.getExamId().toString())
                    .addValue("id", examAccommodation.getId().toString())
                    .addValue("segmentKey", examAccommodation.getSegmentKey())
                    .addValue("type", examAccommodation.getType())
                    .addValue("visible", examAccommodation.isVisible())
                    .addValue("studentControlled", examAccommodation.isStudentControlled())
                    .addValue("disabledOnGuestSession", examAccommodation.isDisabledOnGuestSession())
                    .addValue("defaultAccommodation", examAccommodation.isDefaultAccommodation())
                    .addValue("allowCombine", examAccommodation.isAllowCombine())
                    .addValue("dependsOn", examAccommodation.getDependsOn())
                    .addValue("sortOrder", examAccommodation.getSortOrder())
                    .addValue("code", examAccommodation.getCode())
                    .addValue("allowChange", examAccommodation.isAllowChange())
                    .addValue("value", examAccommodation.getValue())
                    .addValue("segmentPosition", examAccommodation.getSegmentPosition())
                    .addValue("description", examAccommodation.getDescription())
                    .addValue("functional", examAccommodation.isFunctional())
                    .addValue("createdAt", createdAt);
            })
            .toArray(SqlParameterSource[]::new);

        jdbcTemplate.batchUpdate(SQL, parameters);

        update(createdAccommodations.toArray(new ExamAccommodation[createdAccommodations.size()]));
    }

    @Override
    public void update(final ExamAccommodation... examAccommodation) {
        updateEvent(mapJodaInstantToTimestamp(Instant.now()), examAccommodation);
    }

    private void updateEvent(final Timestamp createdAt, final ExamAccommodation... examAccommodations) {
        final String SQL = "INSERT INTO exam_accommodation_event(" +
            "exam_accommodation_id, " +
            "exam_id, " +
            "denied_at, " +
            "deleted_at, " +
            "selectable," +
            "custom," +
            "total_type_count," +
            "created_at) \n" +
            "VALUES(" +
            ":examAccommodationId, " +
            ":examId, " +
            ":deniedAt, " +
            ":deletedAt, " +
            ":selectable," +
            ":custom," +
            ":totalTypeCount," +
            ":createdAt);";

        final SqlParameterSource[] parameterSources = new SqlParameterSource[examAccommodations.length];

        for (int i = 0; i < parameterSources.length; i++) {
            final ExamAccommodation examAccommodation = examAccommodations[i];
            final SqlParameterSource parameters = new MapSqlParameterSource("examAccommodationId", examAccommodation.getId().toString())
                .addValue("examId", examAccommodation.getExamId().toString())
                .addValue("deniedAt", mapJodaInstantToTimestamp(examAccommodation.getDeniedAt()))
                .addValue("selectable", examAccommodation.isSelectable())
                .addValue("totalTypeCount", examAccommodation.getTotalTypeCount())
                .addValue("custom", examAccommodation.isCustom())
                .addValue("deletedAt", mapJodaInstantToTimestamp(examAccommodation.getDeletedAt()))
                .addValue("createdAt", createdAt);

            parameterSources[i] = parameters;
        }

        jdbcTemplate.batchUpdate(SQL, parameterSources);
    }

    @Override
    public void delete(final List<ExamAccommodation> accommodations) {
        final Instant deletedAt = Instant.now();

        final List<ExamAccommodation> accommodationsToDelete = accommodations.stream()
            .map(accommodation -> ExamAccommodation.Builder
                .fromExamAccommodation(accommodation)
                .withDeletedAt(deletedAt)
                .build())
            .collect(Collectors.toList());

        updateEvent(mapJodaInstantToTimestamp(deletedAt),
            accommodationsToDelete.toArray(new ExamAccommodation[accommodationsToDelete.size()]));
    }
}
