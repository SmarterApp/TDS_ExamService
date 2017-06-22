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

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import tds.common.data.mapping.ResultSetMapperUtility;
import tds.exam.ExamineeAttribute;
import tds.exam.ExamineeContext;
import tds.exam.ExamineeRelationship;
import tds.exam.repositories.ExamineeQueryRepository;

@Repository
public class ExamineeQueryRepositoryImpl implements ExamineeQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ExamineeQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public List<ExamineeAttribute> findAllAttributes(final UUID examId) {
        return findAllAttributes(examId, null);
    }

    @Override
    public List<ExamineeAttribute> findAllAttributes(final UUID examId, final ExamineeContext context) {
        final SqlParameterSource parameters = new MapSqlParameterSource("examId", examId.toString())
            .addValue("context", context != null ? context.toString() : null);

        String SQL =
            "SELECT \n" +
                "   attribute.id, \n" +
                "   attribute.exam_id, \n" +
                "   attribute.context, \n" +
                "   attribute.attribute_name, \n" +
                "   attribute.attribute_value, \n" +
                "   attribute.created_at \n" +
                "FROM \n" +
                "   examinee_attribute attribute \n" +
                "JOIN ( \n" +
                "   SELECT \n" +
                "       MAX(id) AS id \n" +
                "   FROM \n" +
                "       examinee_attribute \n" +
                "   WHERE \n" +
                "       exam_id = :examId \n";

        if (context != null) {
            SQL += "   AND context = :context \n";
        }

        SQL +=  "   GROUP BY \n" +
                "       exam_id, \n" +
                "       context, \n" +
                "       attribute_name \n" +
                ") most_recent_record \n" +
                "   ON \n" +
                "       most_recent_record.id = attribute.id \n" +
                "WHERE \n" +
                "   attribute.exam_id = :examId	\n";

        // If context isn't included, just fetch all records for this exam
        if (context != null) {
            SQL += "   AND attribute.context = :context";
        }

        return jdbcTemplate.query(SQL, parameters, (rs, r) -> new ExamineeAttribute.Builder()
            .withId(rs.getLong("id"))
            .withExamId(UUID.fromString(rs.getString("exam_id")))
            .withContext(ExamineeContext.fromType(rs.getString("context")))
            .withName(rs.getString("attribute_name"))
            .withValue(rs.getString("attribute_value"))
            .withCreatedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(rs, "created_at"))
            .build());
    }

    @Override
    public List<ExamineeRelationship> findAllRelationships(final UUID examId) {
        return findAllRelationships(examId, null);
    }

    @Override
    public List<ExamineeRelationship> findAllRelationships(final UUID examId, final ExamineeContext context) {
        final SqlParameterSource parameters = new MapSqlParameterSource("examId", examId.toString())
            .addValue("context", context != null ? context.toString() : null);

        String SQL =
            "SELECT \n" +
                "   relationship.id, \n" +
                "   relationship.exam_id, \n" +
                "   relationship.context, \n" +
                "   relationship.attribute_name, \n" +
                "   relationship.attribute_value, \n" +
                "   relationship.attribute_relationship, \n" +
                "   relationship.created_at \n" +
                "FROM \n" +
                "   examinee_relationship relationship \n" +
                "JOIN ( \n" +
                "   SELECT \n" +
                "       MAX(id) AS id \n" +
                "   FROM \n" +
                "       examinee_relationship \n" +
                "   WHERE \n" +
                "       exam_id = :examId \n";

        if (context != null) {
            SQL += "   AND context = :context \n";
        }

        SQL +=  "   GROUP BY \n" +
                "       exam_id, \n" +
                "       context, \n" +
                "       attribute_name, \n" +
                "       attribute_relationship \n" +
                ") most_recent_record \n" +
                "   ON \n" +
                "       most_recent_record.id = relationship.id \n" +
                "WHERE \n" +
                "   relationship.exam_id = :examId \n";

        if (context != null) {
            SQL +=  "   AND relationship.context = :context";
        }

        return jdbcTemplate.query(SQL, parameters, (rs, r) -> new ExamineeRelationship.Builder()
            .withId(rs.getLong("id"))
            .withExamId(UUID.fromString(rs.getString("exam_id")))
            .withContext(ExamineeContext.fromType(rs.getString("context")))
            .withName(rs.getString("attribute_name"))
            .withValue(rs.getString("attribute_value"))
            .withType(rs.getString("attribute_relationship"))
            .withCreatedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(rs, "created_at"))
            .build());
    }
}
