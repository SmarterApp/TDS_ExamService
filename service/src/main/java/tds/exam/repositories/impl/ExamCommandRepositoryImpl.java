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
import java.util.stream.Stream;

import tds.common.data.CreateRecordException;
import tds.exam.Exam;
import tds.exam.repositories.ExamCommandRepository;

import static tds.common.data.mapping.ResultSetMapperUtility.mapJodaInstantToTimestamp;
import static tds.common.data.mysql.UuidAdapter.getBytesFromUUID;

@Repository
class ExamCommandRepositoryImpl implements ExamCommandRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    ExamCommandRepositoryImpl(@Qualifier("commandJdbcTemplate") final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(final Exam exam) {
        SqlParameterSource examParameters = new MapSqlParameterSource("id", exam.getId().toString())
            .addValue("clientName", exam.getClientName())
            .addValue("environment", exam.getEnvironment())
            .addValue("subject", exam.getSubject())
            .addValue("loginSsid", exam.getLoginSSID())
            .addValue("studentId", exam.getStudentId())
            .addValue("studentName", exam.getStudentName())
            .addValue("assessmentId", exam.getAssessmentId())
            .addValue("assessmentKey", exam.getAssessmentKey())
            .addValue("assessmentWindowId", exam.getAssessmentWindowId())
            .addValue("assessmentAlgorithm", exam.getAssessmentAlgorithm())
            .addValue("segmented", exam.isSegmented())
            .addValue("joinedAt", mapJodaInstantToTimestamp(exam.getDateJoined()))
            .addValue("msb", exam.isMultiStageBraille())
            .addValue("createdAt", mapJodaInstantToTimestamp(Instant.now()));

        String examInsertSQL = "INSERT INTO exam \n" +
            "(\n" +
            "  id,\n" +
            "  client_name, \n" +
            "  environment,\n" +
            "  subject,\n" +
            "  login_ssid,\n" +
            "  student_id,\n" +
            "  student_name,\n" +
            "  assessment_id,\n" +
            "  assessment_key,\n" +
            "  assessment_window_id,\n" +
            "  assessment_algorithm,\n" +
            "  segmented,\n" +
            "  msb,\n" +
            "  joined_at, \n" +
            "  created_at \n" +
            ")\n" +
            "VALUES\n" +
            "(\n" +
            "  :id,\n" +
            "  :clientName,\n" +
            "  :environment,\n" +
            "  :subject,\n" +
            "  :loginSsid,\n" +
            "  :studentId,\n" +
            "  :studentName,\n" +
            "  :assessmentId,\n" +
            "  :assessmentKey,\n" +
            "  :assessmentWindowId,\n" +
            "  :assessmentAlgorithm,\n" +
            "  :segmented,\n" +
            "  :msb,\n" +
            "  :joinedAt, \n" +
            "  :createdAt \n" +
            ");";

        int insertCount = jdbcTemplate.update(examInsertSQL, examParameters);

        if (insertCount != 1) {
            throw new CreateRecordException("Failed to insert exam");
        }

        update(exam);
    }

    @Override
    public void update(final Exam... exams) {
        final Timestamp now = mapJodaInstantToTimestamp(org.joda.time.Instant.now());

        SqlParameterSource[] batchParameters = Stream.of(exams)
            .map(exam -> new MapSqlParameterSource("examId", exam.getId().toString())
                .addValue("attempts", exam.getAttempts())
                .addValue("sessionId", exam.getSessionId().toString())
                .addValue("status", exam.getStatus().getCode())
                .addValue("statusChangedAt", mapJodaInstantToTimestamp(exam.getStatusChangedAt()))
                .addValue("browserId", getBytesFromUUID(exam.getBrowserId()))
                .addValue("maxItems", exam.getMaxItems())
                .addValue("languageCode", exam.getLanguageCode())
                .addValue("statusChangeReason", exam.getStatusChangeReason())
                .addValue("changedAt", now)
                .addValue("deletedAt", mapJodaInstantToTimestamp(exam.getDeletedAt()))
                .addValue("completedAt", mapJodaInstantToTimestamp(exam.getCompletedAt()))
                .addValue("scoredAt", mapJodaInstantToTimestamp(exam.getScoredAt()))
                .addValue("expiresAt", mapJodaInstantToTimestamp(exam.getExpiresAt()))
                .addValue("abnormalStarts", exam.getAbnormalStarts())
                .addValue("waitingForSegmentApprovalPosition", exam.getWaitingForSegmentApprovalPosition())
                .addValue("currentSegmentPosition", exam.getCurrentSegmentPosition())
                .addValue("customAccommodations", exam.isCustomAccommodations())
                .addValue("startedAt", mapJodaInstantToTimestamp(exam.getStartedAt()))
                .addValue("browserUserAgent", exam.getBrowserUserAgent())
                .addValue("restartsAndResumptions", exam.getRestartsAndResumptions())
                .addValue("resumptions", exam.getResumptions())
                .addValue("createdAt", now))
            .toArray(MapSqlParameterSource[]::new);

        final String SQL =
            "INSERT INTO exam_event (\n" +
                "exam_id, \n" +
                "attempts, \n" +
                "max_items, \n" +
                "language_code, \n" +
                "expires_at, \n" +
                "session_id, \n" +
                "browser_id, \n" +
                "status, \n" +
                "status_changed_at, \n" +
                "status_change_reason, \n" +
                "changed_at, \n" +
                "deleted_at, \n" +
                "completed_at, \n" +
                "scored_at, \n" +
                "started_at, \n" +
                "waiting_for_segment_approval_position, \n" +
                "current_segment_position, \n" +
                "custom_accommodations, \n" +
                "abnormal_starts, \n" +
                "browser_user_agent, \n" +
                "resumptions, \n" +
                "restarts_and_resumptions, \n" +
                "created_at \n" +
                ") \n" +
                "VALUES \n" +
                "( \n" +
                ":examId, \n" +
                ":attempts, \n" +
                ":maxItems, \n" +
                ":languageCode, \n" +
                ":expiresAt, \n" +
                ":sessionId, \n" +
                ":browserId, \n" +
                ":status, \n" +
                ":statusChangedAt, \n" +
                ":statusChangeReason, \n" +
                ":changedAt, \n" +
                ":deletedAt, \n" +
                ":completedAt, \n" +
                ":scoredAt, \n" +
                ":startedAt, \n" +
                ":waitingForSegmentApprovalPosition,\n" +
                ":currentSegmentPosition, \n" +
                ":customAccommodations, \n" +
                ":abnormalStarts, \n" +
                ":browserUserAgent, \n" +
                ":resumptions, \n" +
                ":restartsAndResumptions, \n" +
                ":createdAt \n" +
                ")";

        jdbcTemplate.batchUpdate(SQL, batchParameters);
    }
}
