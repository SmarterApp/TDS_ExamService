package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.exam.ExamAccommodation;
import tds.exam.models.ExamAccommodationFilter;
import tds.exam.repositories.ExamAccommodationQueryRepository;

import static tds.common.data.mapping.ResultSetMapperUtility.mapTimestampToJodaInstant;

@Repository
public class ExamAccommodationQueryRepositoryImpl implements ExamAccommodationQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final RowMapper<ExamAccommodation> accommodationRowMapper = new AccommodationRowMapper();

    @Autowired
    public ExamAccommodationQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") final NamedParameterJdbcTemplate queryJdbcTemplate) {
        this.jdbcTemplate = queryJdbcTemplate;
    }

    @Override
    public List<ExamAccommodation> findAccommodations(final UUID examId, final String segmentKey, final String[] accommodationTypes) {
        final MapSqlParameterSource parameters = new MapSqlParameterSource("examId", examId.toString())
            .addValue("segmentKey", segmentKey);

        String SQL =
            "SELECT \n" +
                "   ea.id, \n" +
                "   ea.exam_id, \n" +
                "   ea.segment_key, \n" +
                "   ea.`type`, \n" +
                "   ea.code, \n" +
                "   ea.description, \n" +
                "   eae.denied_at, \n" +
                "   ea.created_at, \n" +
                "   ea.allow_change, \n" +
                "   ea.value, \n" +
                "   ea.segment_position, \n" +
                "   ea.visible, \n" +
                "   ea.student_controlled, \n" +
                "   ea.disabled_on_guest_session, \n" +
                "   ea.default_accommodation, \n" +
                "   ea.allow_combine, \n" +
                "   ea.depends_on, \n" +
                "   ea.sort_order, \n" +
                "   ea.functional, \n" +
                "   eae.selectable, \n" +
                "   eae.total_type_count, \n" +
                "   eae.custom \n" +
                "FROM \n" +
                "   exam_accommodation ea \n" +
                "JOIN ( \n" +
                "   SELECT \n" +
                "       exam_accommodation_id, \n" +
                "       MAX(id) AS id \n" +
                "   FROM \n" +
                "       exam_accommodation_event \n" +
                "   WHERE \n" +
                "       exam_id = :examId \n" +
                "   GROUP BY exam_accommodation_id \n" +
                ") last_event \n" +
                "  ON ea.id = last_event.exam_accommodation_id \n" +
                "JOIN exam_accommodation_event eae \n" +
                "  ON last_event.id = eae.id \n" +
                "WHERE \n" +
                "   ea.exam_id = :examId \n" +
                "   AND ea.segment_key = :segmentKey \n" +
                "   AND eae.deleted_at IS NULL";

        if (accommodationTypes.length > 0) {
            parameters.addValue("accommodationTypes", Arrays.asList(accommodationTypes));
            SQL += "\n   AND ea.`type` IN (:accommodationTypes)";
        }

        return jdbcTemplate.query(SQL,
            parameters,
            accommodationRowMapper);
    }

    @Override
    public List<ExamAccommodation> findAccommodations(final UUID... examIds) {
        return getAccommodations(false, examIds);
    }

    @Override
    public List<ExamAccommodation> findApprovedAccommodations(final UUID... examIds) {
        return getAccommodations(true, examIds);
    }

    private List<ExamAccommodation> getAccommodations(final boolean excludeDenied, final UUID... examIds) {
        final SqlParameterSource parameters = new MapSqlParameterSource("examIds",
            Arrays.stream(examIds).map(UUID::toString).collect(Collectors.toSet()));

        String SQL =
            "SELECT \n" +
                "   ea.id, \n" +
                "   ea.exam_id, \n" +
                "   ea.segment_key, \n" +
                "   ea.`type`, \n" +
                "   ea.code, \n" +
                "   ea.description, \n" +
                "   eae.denied_at, \n" +
                "   ea.created_at, \n" +
                "   ea.allow_change, \n" +
                "   ea.value, \n" +
                "   ea.visible, \n" +
                "   ea.student_controlled, \n" +
                "   ea.disabled_on_guest_session, \n" +
                "   ea.default_accommodation, \n" +
                "   ea.allow_combine, \n" +
                "   ea.depends_on, \n" +
                "   ea.sort_order, \n" +
                "   ea.functional, \n" +
                "   ea.segment_position, \n" +
                "   eae.total_type_count, \n" +
                "   eae.selectable, \n" +
                "   eae.custom \n" +
                "FROM \n" +
                "   exam_accommodation ea \n" +
                "JOIN ( \n" +
                "   SELECT \n" +
                "       exam_accommodation_id, \n" +
                "       MAX(id) AS id \n" +
                "   FROM \n" +
                "       exam_accommodation_event \n" +
                "   WHERE \n" +
                "       exam_id IN (:examIds) \n" +
                "   GROUP BY exam_accommodation_id \n" +
                ") last_event \n" +
                "  ON ea.id = last_event.exam_accommodation_id \n" +
                "JOIN exam_accommodation_event eae \n" +
                "  ON last_event.id = eae.id \n" +
                "WHERE \n" +
                "   ea.exam_id IN (:examIds) \n" +
                "   AND eae.deleted_at IS NULL";

        if (excludeDenied) {
            SQL += "\n AND eae.denied_at IS NULL";
        }

        return jdbcTemplate.query(SQL,
            parameters,
            accommodationRowMapper);
    }

    @Override
    public List<ExamAccommodation> findAccommodations(final UUID examId, final Collection<ExamAccommodationFilter> examAccommodationFilters) {
        final MapSqlParameterSource parameters = new MapSqlParameterSource("examId", examId.toString());

        StringBuilder SQL =
            new StringBuilder("SELECT \n" +
                "   ea.id, \n" +
                "   ea.exam_id, \n" +
                "   ea.segment_key, \n" +
                "   ea.`type`, \n" +
                "   ea.code, \n" +
                "   ea.description, \n" +
                "   eae.denied_at, \n" +
                "   ea.created_at, \n" +
                "   ea.allow_change, \n" +
                "   ea.value, \n" +
                "   ea.segment_position, \n" +
                "   ea.visible, \n" +
                "   ea.student_controlled, \n" +
                "   ea.disabled_on_guest_session, \n" +
                "   ea.default_accommodation, \n" +
                "   ea.allow_combine, \n" +
                "   ea.depends_on, \n" +
                "   ea.sort_order, \n" +
                "   ea.functional, \n" +
                "   eae.selectable, \n" +
                "   eae.total_type_count, \n" +
                "   eae.custom \n" +
                "FROM \n" +
                "   exam_accommodation ea \n" +
                "JOIN ( \n" +
                "   SELECT \n" +
                "       exam_accommodation_id, \n" +
                "       MAX(id) AS id \n" +
                "   FROM \n" +
                "       exam_accommodation_event \n" +
                "   WHERE \n" +
                "       exam_id = :examId \n" +
                "   GROUP BY exam_accommodation_id \n" +
                ") last_event \n" +
                "  ON ea.id = last_event.exam_accommodation_id \n" +
                "JOIN exam_accommodation_event eae \n" +
                "  ON last_event.id = eae.id \n" +
                "WHERE \n" +
                "   ea.exam_id = :examId \n" +
                "   AND eae.deleted_at IS NULL");

        Iterator<ExamAccommodationFilter> iterator = examAccommodationFilters.iterator();
        int counter = 1;
        while(iterator.hasNext()) {
            ExamAccommodationFilter identifier = iterator.next();

            if(counter == 1) {
                SQL.append(" AND ( \n");
            }

            SQL.append(String.format("( ea.code = :code%d AND ea.`type` = :type%d ) \n", counter, counter));
            parameters.addValue("code" + counter, identifier.getCode());
            parameters.addValue("type" + counter, identifier.getType());

            if (examAccommodationFilters.size() == counter) {
                SQL.append(" \n ) \n");
            } else {
                SQL.append(" OR \n ");
            }

            counter++;
        }

        return jdbcTemplate.query(SQL.toString(),
            parameters,
            accommodationRowMapper);
    }

    private static class AccommodationRowMapper implements RowMapper<ExamAccommodation> {
        @Override
        public ExamAccommodation mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            return new ExamAccommodation.Builder(UUID.fromString(rs.getString("id")))
                .withExamId(UUID.fromString(rs.getString("exam_id")))
                .withSegmentKey(rs.getString("segment_key"))
                .withType(rs.getString("type"))
                .withCode(rs.getString("code"))
                .withDescription(rs.getString("description"))
                .withDeniedAt(mapTimestampToJodaInstant(rs, "denied_at"))
                .withCreatedAt(mapTimestampToJodaInstant(rs, "created_at"))
                .withSelectable(rs.getBoolean("selectable"))
                .withAllowChange(rs.getBoolean("allow_change"))
                .withValue(rs.getString("value"))
                .withSegmentPosition(rs.getInt("segment_position"))
                .withTotalTypeCount(rs.getInt("total_type_count"))
                .withCustom(rs.getBoolean("custom"))
                .withVisible(rs.getBoolean("visible"))
                .withStudentControlled(rs.getBoolean("student_controlled"))
                .withDisabledOnGuestSession(rs.getBoolean("disabled_on_guest_session"))
                .withDefaultAccommodation(rs.getBoolean("default_accommodation"))
                .withAllowCombine(rs.getBoolean("allow_combine"))
                .withSortOrder(rs.getInt("sort_order"))
                .withDependsOn(rs.getString("depends_on"))
                .withFunctional(rs.getBoolean("functional"))
                .build();
        }
    }
}
