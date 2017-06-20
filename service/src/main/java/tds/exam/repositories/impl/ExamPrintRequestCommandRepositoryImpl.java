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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import tds.exam.ExamPrintRequest;
import tds.exam.ExamPrintRequestStatus;
import tds.exam.repositories.ExamPrintRequestCommandRepository;

import static tds.common.data.mapping.ResultSetMapperUtility.mapJodaInstantToTimestamp;

@Repository
public class ExamPrintRequestCommandRepositoryImpl implements ExamPrintRequestCommandRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamPrintRequestCommandRepositoryImpl(@Qualifier("commandJdbcTemplate") final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(final ExamPrintRequest examPrintRequest) {
        final SqlParameterSource params = new MapSqlParameterSource("id", examPrintRequest.getId().toString())
            .addValue("examId", examPrintRequest.getExamId().toString())
            .addValue("sessionId", examPrintRequest.getSessionId().toString())
            .addValue("type", examPrintRequest.getType())
            .addValue("value", examPrintRequest.getValue())
            .addValue("itemPosition", examPrintRequest.getItemPosition())
            .addValue("pagePosition", examPrintRequest.getPagePosition())
            .addValue("parameters", examPrintRequest.getParameters())
            .addValue("description", examPrintRequest.getDescription())
            .addValue("createdAt", mapJodaInstantToTimestamp(Instant.now()));

        final String examPrintRequestSQL =
            "INSERT INTO \n" +
                "exam.exam_print_request ( \n" +
                "   id, \n" +
                "   exam_id, \n" +
                "   session_id, \n" +
                "   type, \n" +
                "   value, \n" +
                "   item_position, \n" +
                "   page_position, \n" +
                "   parameters, \n" +
                "   description, \n" +
                "   created_at \n" +
                ") \n" +
                "VALUES ( \n" +
                "   :id, \n" +
                "   :examId, \n" +
                "   :sessionId, \n" +
                "   :type, \n" +
                "   :value, \n" +
                "   :itemPosition, \n" +
                "   :pagePosition, \n" +
                "   :parameters, \n" +
                "   :description, \n" +
                "   :createdAt \n" +
                ")";

        jdbcTemplate.update(examPrintRequestSQL, params);
        update(examPrintRequest);
    }

    @Override
    public void update(final ExamPrintRequest examPrintRequest) {
        final SqlParameterSource params = new MapSqlParameterSource("examRequestId", examPrintRequest.getId().toString())
            .addValue("status", examPrintRequest.getStatus() != null ? examPrintRequest.getStatus().name() : ExamPrintRequestStatus.SUBMITTED.name())
            .addValue("createdAt", mapJodaInstantToTimestamp(Instant.now()))
            .addValue("reasonDenied", examPrintRequest.getReasonDenied());

        final String updateExamPrintRequestSQL =
            "INSERT INTO \n" +
                "exam.exam_print_request_event ( \n" +
                "   exam_print_request_id, \n" +
                "   status, \n" +
                "   created_at, \n" +
                "   reason_denied \n" +
                ") \n" +
                "VALUES ( \n" +
                "   :examRequestId, \n" +
                "   :status, \n" +
                "   :createdAt, \n" +
                "   :reasonDenied \n" +
                ")";

        jdbcTemplate.update(updateExamPrintRequestSQL, params);
    }
}
