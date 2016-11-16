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
        final SqlParameterSource parameters = new MapSqlParameterSource("examId", UuidAdapter.getBytesFromUUID(examId))
            .addValue("segmentKey", segmentKey)
            .addValue("accommodationTypes", Arrays.asList(accommodationTypes));

        final String SQL =
            "SELECT \n" +
            "   id, \n" +
            "   exam_id, \n" +
            "   segment_key, \n" +
            "   `type`, \n" +
            "   code, \n" +
            "   description, \n" +
            "   denied_at, \n" +
            "   created_at \n" +
            "FROM \n" +
            "   exam_accommodations \n" +
            "WHERE \n" +
            "   exam_id = :examId \n" +
            "   AND segment_key = :segmentKey \n" +
            "   AND `type` IN (:accommodationTypes)";

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
                .build();
        }
    }
}
