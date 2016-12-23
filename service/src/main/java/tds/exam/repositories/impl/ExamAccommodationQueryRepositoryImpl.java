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
import java.util.List;
import java.util.UUID;

import tds.common.data.mysql.UuidAdapter;
import tds.exam.ExamAccommodation;
import tds.exam.repositories.ExamAccommodationQueryRepository;

import static tds.common.data.mapping.ResultSetMapperUtility.mapTimestampToJodaInstant;

@Repository
public class ExamAccommodationQueryRepositoryImpl implements ExamAccommodationQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamAccommodationQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") NamedParameterJdbcTemplate queryJdbcTemplate) {
        this.jdbcTemplate = queryJdbcTemplate;
    }

    @Override
    public List<ExamAccommodation> findAccommodations(UUID examId, String segmentKey, String[] accommodationTypes) {
        final MapSqlParameterSource parameters = new MapSqlParameterSource("examId", UuidAdapter.getBytesFromUUID(examId))
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
            "   eae.selectable, \n" +
                "   eae.total_type_count \n" +
            "FROM \n" +
            "   exam_accommodation ea \n" +
            "JOIN ( \n" +
            "   SELECT \n" +
            "       exam_accommodation_id, \n" +
            "       MAX(id) AS id \n" +
            "   FROM \n" +
            "       exam_accommodation_event \n" +
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
            SQL += "   AND ea.`type` IN (:accommodationTypes)";
        }

        return jdbcTemplate.query(SQL,
                parameters,
                new AccommodationRowMapper());
    }

    @Override
    public List<ExamAccommodation> findAccommodations(UUID examId) {
        return getAccommodations(examId, false);
    }

    @Override
    public List<ExamAccommodation> findApprovedAccommodations(UUID examId) {
        return getAccommodations(examId, true);
    }

    private List<ExamAccommodation> getAccommodations(UUID examId, boolean excludeDenied) {
        final SqlParameterSource parameters = new MapSqlParameterSource("examId", UuidAdapter.getBytesFromUUID(examId));

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
                "   eae.selectable, \n" +
                "   eae.total_type_count \n" +
                "FROM \n" +
                "   exam_accommodation ea \n" +
                "JOIN ( \n" +
                "   SELECT \n" +
                "       exam_accommodation_id, \n" +
                "       MAX(id) AS id \n" +
                "   FROM \n" +
                "       exam_accommodation_event \n" +
                "   GROUP BY exam_accommodation_id \n" +
                ") last_event \n" +
                "  ON ea.id = last_event.exam_accommodation_id \n" +
                "JOIN exam_accommodation_event eae \n" +
                "  ON last_event.id = eae.id \n" +
                "WHERE \n" +
                "   ea.exam_id = :examId" +
                "   AND eae.deleted_at IS NULL";

        if (excludeDenied) {
            SQL += "\n AND eae.denied_at IS NULL";
        }

        return jdbcTemplate.query(SQL,
            parameters,
            new AccommodationRowMapper());
    }

    private class AccommodationRowMapper implements RowMapper<ExamAccommodation> {
        @Override
        public ExamAccommodation mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new ExamAccommodation.Builder()
                .withId(rs.getLong("id"))
                .withExamId(UuidAdapter.getUUIDFromBytes(rs.getBytes("exam_id")))
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
                .build();
        }
    }
}
