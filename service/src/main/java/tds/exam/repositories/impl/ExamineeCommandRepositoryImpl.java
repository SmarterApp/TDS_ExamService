package tds.exam.repositories.impl;

import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.stream.Stream;

import tds.common.data.mapping.ResultSetMapperUtility;
import tds.exam.ExamineeAttribute;
import tds.exam.ExamineeRelationship;
import tds.exam.repositories.ExamineeCommandRepository;

@Repository
public class ExamineeCommandRepositoryImpl implements ExamineeCommandRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ExamineeCommandRepositoryImpl(@Qualifier("commandJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insertAttributes(ExamineeAttribute... attributes) {
        final SqlParameterSource[] batchParameters = Stream.of(attributes)
            .map(attribute -> new MapSqlParameterSource("examId", attribute.getExamId().toString())
                .addValue("context", attribute.getContext().toString())
                .addValue("attributeName", attribute.getName())
                .addValue("attributeValue", attribute.getValue())
                .addValue("createdAt", ResultSetMapperUtility.mapJodaInstantToTimestamp(Instant.now())))
            .toArray(MapSqlParameterSource[]::new);

        final String SQL =
            "INSERT INTO examinee_attribute( \n" +
                "   exam_id, \n" +
                "   context, \n" +
                "   attribute_name, \n" +
                "   attribute_value, \n" +
                "   created_at) \n" +
                "VALUES (\n" +
                "   :examId, \n" +
                "   :context, \n" +
                "   :attributeName, \n" +
                "   :attributeValue, \n" +
                "   :createdAt)";

        jdbcTemplate.batchUpdate(SQL, batchParameters);
    }

    @Override
    public void insertRelationships(ExamineeRelationship... relationships) {
        final SqlParameterSource[] batchParameters = Stream.of(relationships)
            .map(relationship -> new MapSqlParameterSource("examId", relationship.getExamId().toString())
                .addValue("attributeName", relationship.getName())
                .addValue("attributeValue", relationship.getValue())
                .addValue("attributeRelationship", relationship.getType())
                .addValue("context", relationship.getContext().toString())
                .addValue("createdAt", ResultSetMapperUtility.mapJodaInstantToTimestamp(Instant.now())))
            .toArray(MapSqlParameterSource[]::new);

        final String SQL =
            "INSERT INTO examinee_relationship( \n" +
                "   exam_id, \n" +
                "   attribute_name, \n" +
                "   attribute_value, \n" +
                "   attribute_relationship, \n" +
                "   context, \n" +
                "   created_at) \n" +
                "VALUES ( \n" +
                "   :examId, \n" +
                "   :attributeName, \n" +
                "   :attributeValue, \n" +
                "   :attributeRelationship, \n" +
                "   :context, \n" +
                "   :createdAt)";

        jdbcTemplate.batchUpdate(SQL, batchParameters);
    }
}
