package tds.exam.repositories.impl;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import tds.common.data.mapping.ResultSetMapperUtility;
import tds.common.data.mysql.UuidAdapter;
import tds.exam.Accommodation;
import tds.exam.repositories.AccommodationQueryRepository;

@Repository
public class AccommodationQueryRepositoryImpl implements AccommodationQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public AccommodationQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") NamedParameterJdbcTemplate queryJdbcTemplate) {
        this.jdbcTemplate = queryJdbcTemplate;
    }

    @Override
    public List<Accommodation> findAccommodations(UUID examId, String segmentId, String[] accommodationTypes) {
        final SqlParameterSource parameters = new MapSqlParameterSource("examId", UuidAdapter.getBytesFromUUID(examId))
            .addValue("segmentId", segmentId)
            .addValue("accommodationTypes", Arrays.asList(accommodationTypes));

        final String SQL =
            "SELECT \n" +
            "   id, \n" +
            "   exam_id, \n" +
            "   segment_id, \n" +
            "   `type`, \n" +
            "   code, \n" +
            "   description, \n" +
            "   denied_at, \n" +
            "   created_at \n" +
            "FROM \n" +
            "   exam_accommodations \n" +
            "WHERE \n" +
            "   exam_id = :examId \n" +
            "   AND segment_id = :segmentId \n" +
            "   AND `type` IN (:accommodationTypes)";

        return jdbcTemplate.query(SQL,
                parameters,
                new AccommodationRowMapper());
    }

    private class AccommodationRowMapper implements RowMapper<Accommodation> {
        @Override
        public Accommodation mapRow(ResultSet rs, int rowNum) throws SQLException {
            return new Accommodation.Builder()
                .withId(rs.getLong("id"))
                .withExamId(UuidAdapter.getUUIDFromBytes(rs.getBytes("exam_id")))
                .withSegmentId(rs.getString("segment_id"))
                .withType(rs.getString("type"))
                .withCode(rs.getString("code"))
                .withDescription(rs.getString("description"))
                .withDeniedAt(ResultSetMapperUtility.mapTimeStampToInstant(rs, "denied_at"))
                .withCreatedAt(ResultSetMapperUtility.mapTimeStampToInstant(rs, "created_at"))
                .build();
        }
    }
}
