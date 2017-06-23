/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

package tds.exam.repositories.impl;

import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.stream.Stream;

import tds.common.data.mapping.ResultSetMapperUtility;
import tds.exam.ExamineeAttribute;
import tds.exam.ExamineeRelationship;
import tds.exam.repositories.ExamineeCommandRepository;

@Repository
public class ExamineeCommandRepositoryImpl implements ExamineeCommandRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ExamineeCommandRepositoryImpl(@Qualifier("commandJdbcTemplate") final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insertAttributes(final ExamineeAttribute... attributes) {
        final Timestamp createdAt = ResultSetMapperUtility.mapJodaInstantToTimestamp(Instant.now());
        final SqlParameterSource[] batchParameters = Stream.of(attributes)
            .map(attribute -> new MapSqlParameterSource("examId", attribute.getExamId().toString())
                .addValue("context", attribute.getContext().toString())
                .addValue("attributeName", attribute.getName())
                .addValue("attributeValue", attribute.getValue())
                .addValue("createdAt", createdAt))
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
    public void insertRelationships(final ExamineeRelationship... relationships) {
        final Timestamp createdAt = ResultSetMapperUtility.mapJodaInstantToTimestamp(Instant.now());
        final SqlParameterSource[] batchParameters = Stream.of(relationships)
            .map(relationship -> new MapSqlParameterSource("examId", relationship.getExamId().toString())
                .addValue("attributeName", relationship.getName())
                .addValue("attributeValue", relationship.getValue())
                .addValue("attributeRelationship", relationship.getType())
                .addValue("context", relationship.getContext().toString())
                .addValue("createdAt", createdAt))
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
