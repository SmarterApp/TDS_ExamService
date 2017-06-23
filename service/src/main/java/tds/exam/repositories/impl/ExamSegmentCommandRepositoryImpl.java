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

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import tds.common.data.mapping.ResultSetMapperUtility;
import tds.exam.ExamSegment;
import tds.exam.repositories.ExamSegmentCommandRepository;

import static tds.common.data.mapping.ResultSetMapperUtility.mapJodaInstantToTimestamp;

/**
 * Repository responsible for writing to the exam_segment and exam_segment_event table.
 */
@Repository
public class ExamSegmentCommandRepositoryImpl implements ExamSegmentCommandRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamSegmentCommandRepositoryImpl(final @Qualifier("commandJdbcTemplate") NamedParameterJdbcTemplate commandJdbcTemplate) {
        this.jdbcTemplate = commandJdbcTemplate;
    }

    /**
     * @inheritDoc
     */
    @Override
    public void insert(final List<ExamSegment> segments) {
        final Timestamp createdAt = mapJodaInstantToTimestamp(Instant.now());
        final List<SqlParameterSource> parameterSources = segments.stream()
            .map(segment -> new MapSqlParameterSource("examId", segment.getExamId().toString())
                .addValue("segmentKey", segment.getSegmentKey())
                .addValue("segmentId", segment.getSegmentId())
                .addValue("segmentPosition", segment.getSegmentPosition())
                .addValue("formKey", segment.getFormKey())
                .addValue("formId", segment.getFormId())
                .addValue("algorithm", segment.getAlgorithm().getType())
                .addValue("examItemCount", segment.getExamItemCount())
                .addValue("fieldTestItemCount", segment.getFieldTestItemCount())
                .addValue("formCohort", segment.getFormCohort())
                .addValue("poolCount", segment.getPoolCount())
                .addValue("isSatisfied", segment.isSatisfied())
                .addValue("isPermeable", segment.isPermeable())
                .addValue("restorePermeableOn", segment.getRestorePermeableCondition())
                .addValue("exitedAt", ResultSetMapperUtility.mapJodaInstantToTimestamp(segment.getExitedAt()))
                .addValue("itemPool", String.join(",", segment.getItemPool()))
                .addValue("createdAt", createdAt))
            .collect(Collectors.toList());

        final String segmentQuery =
            "INSERT INTO exam_segment (\n" +
                "   exam_id, \n" +
                "   segment_key, \n" +
                "   segment_id, \n" +
                "   segment_position, \n" +
                "   form_key, \n" +
                "   form_id, \n" +
                "   algorithm, \n" +
                "   exam_item_count, \n" +
                "   field_test_item_count, \n" +
                "   form_cohort, \n" +
                "   pool_count, \n" +
                "   created_at \n" +
                ") \n" +
                "VALUES ( \n" +
                "   :examId, \n" +
                "   :segmentKey, \n" +
                "   :segmentId, \n" +
                "   :segmentPosition, \n" +
                "   :formKey, \n" +
                "   :formId, \n" +
                "   :algorithm, \n" +
                "   :examItemCount, \n" +
                "   :fieldTestItemCount, \n" +
                "   :formCohort, \n" +
                "   :poolCount, \n" +
                "   :createdAt \n" +
                ")";

        jdbcTemplate.batchUpdate(segmentQuery, parameterSources.toArray(new SqlParameterSource[parameterSources.size()]));
        update(segments);
    }

    @Override
    public void update(final ExamSegment segment) {
        update(Collections.singletonList(segment));
    }

    @Override
    public void update(final List<ExamSegment> segments) {
        final List<SqlParameterSource> parameterSources = new ArrayList<>();
        final Timestamp createdAt = mapJodaInstantToTimestamp(Instant.now());
        segments.forEach(segment -> {
            SqlParameterSource parameters = new MapSqlParameterSource(
                "examId", segment.getExamId().toString())
                .addValue("segmentPosition", segment.getSegmentPosition())
                .addValue("isSatisfied", segment.isSatisfied())
                .addValue("isPermeable", segment.isPermeable())
                .addValue("restorePermeableCondition", segment.getRestorePermeableCondition())
                .addValue("exitedAt", ResultSetMapperUtility.mapJodaInstantToTimestamp(segment.getExitedAt()))
                .addValue("itemPool", String.join(",", segment.getItemPool()))
                .addValue("offGradeItems", segment.getOffGradeItems())
                .addValue("createdAt", createdAt);
            parameterSources.add(parameters);
        });

        final String segmentEventQuery =
            "INSERT INTO exam_segment_event (\n" +
                "   exam_id, \n" +
                "   segment_position, \n" +
                "   satisfied, \n" +
                "   permeable, \n" +
                "   restore_permeable_condition, \n" +
                "   exited_at, \n" +
                "   item_pool, \n" +
                "   off_grade_items, \n" +
                "   created_at \n" +
                ") \n" +
                "VALUES ( \n" +
                "   :examId, \n" +
                "   :segmentPosition, \n" +
                "   :isSatisfied, \n" +
                "   :isPermeable, \n" +
                "   :restorePermeableCondition, \n" +
                "   :exitedAt, \n" +
                "   :itemPool, \n" +
                "   :offGradeItems, \n" +
                "   :createdAt \n" +
                ")";

        jdbcTemplate.batchUpdate(segmentEventQuery, parameterSources.toArray(new SqlParameterSource[parameterSources.size()]));
    }
}
