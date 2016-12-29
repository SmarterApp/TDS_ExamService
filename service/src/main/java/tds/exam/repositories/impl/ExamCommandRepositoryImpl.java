package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import tds.common.data.CreateRecordException;
import tds.exam.Exam;
import tds.exam.repositories.ExamCommandRepository;

import static tds.common.data.mapping.ResultSetMapperUtility.mapJodaInstantToTimestamp;
import static tds.common.data.mysql.UuidAdapter.getBytesFromUUID;

@Repository
class ExamCommandRepositoryImpl implements ExamCommandRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    ExamCommandRepositoryImpl(@Qualifier("commandJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(Exam exam) {
        SqlParameterSource examParameters = new MapSqlParameterSource("id", getBytesFromUUID(exam.getId()))
            .addValue("clientName", exam.getClientName())
            .addValue("environment", exam.getEnvironment())
            .addValue("sessionId", getBytesFromUUID(exam.getSessionId()))
            .addValue("subject", exam.getSubject())
            .addValue("loginSsid", exam.getLoginSSID())
            .addValue("studentId", exam.getStudentId())
            .addValue("studentName", exam.getStudentName())
            .addValue("assessmentId", exam.getAssessmentId())
            .addValue("assessmentKey", exam.getAssessmentKey())
            .addValue("assessmentWindowId", exam.getAssessmentWindowId())
            .addValue("assessmentAlgorithm", exam.getAssessmentAlgorithm())
            .addValue("segmented", exam.isSegmented())
            .addValue("dateJoined", mapJodaInstantToTimestamp(exam.getDateJoined()));

        String examInsertSQL = "INSERT INTO exam \n" +
            "(\n" +
            "  id,\n" +
            "  client_name, \n" +
            "  environment,\n" +
            "  session_id,\n" +
            "  subject,\n" +
            "  login_ssid,\n" +
            "  student_id,\n" +
            "  student_name,\n" +
            "  assessment_id,\n" +
            "  assessment_key,\n" +
            "  assessment_window_id,\n" +
            "  assessment_algorithm,\n" +
            "  segmented,\n" +
            "  date_joined\n" +
            ")\n" +
            "VALUES\n" +
            "(\n" +
            "  :id,\n" +
            "  :clientName,\n" +
            "  :environment,\n" +
            "  :sessionId,\n" +
            "  :subject,\n" +
            "  :loginSsid,\n" +
            "  :studentId,\n" +
            "  :studentName,\n" +
            "  :assessmentId,\n" +
            "  :assessmentKey,\n" +
            "  :assessmentWindowId,\n" +
            "  :assessmentAlgorithm,\n" +
            "  :segmented,\n" +
            "  :dateJoined \n" +
            ");";

        int insertCount = jdbcTemplate.update(examInsertSQL, examParameters);

        if (insertCount != 1) {
            throw new CreateRecordException("Failed to insert exam");
        }

        update(exam);
    }

    @Override
    public void update(Exam exam) {
        SqlParameterSource examEventParameters = new MapSqlParameterSource("examId", getBytesFromUUID(exam.getId()))
            .addValue("attempts", exam.getAttempts())
            .addValue("status", exam.getStatus().getStatus())
            .addValue("statusChangeDate", mapJodaInstantToTimestamp(exam.getStatusChangeDate()))
            .addValue("browserId", getBytesFromUUID(exam.getBrowserId()))
            .addValue("maxItems", exam.getMaxItems())
            .addValue("languageCode", exam.getLanguageCode())
            .addValue("statusChangeReason", exam.getStatusChangeReason())
            .addValue("dateChanged", mapJodaInstantToTimestamp(exam.getDateChanged()))
            .addValue("dateDeleted", mapJodaInstantToTimestamp(exam.getDateDeleted()))
            .addValue("dateCompleted", mapJodaInstantToTimestamp(exam.getDateCompleted()))
            .addValue("dateScored", mapJodaInstantToTimestamp(exam.getDateScored()))
            .addValue("expireFrom", mapJodaInstantToTimestamp(exam.getExpireFrom()))
            .addValue("abnormalStarts", exam.getAbnormalStarts())
            .addValue("waitingForSegmentApproval", exam.isWaitingForSegmentApproval())
            .addValue("currentSegmentPosition", exam.getCurrentSegmentPosition())
            .addValue("dateStarted", mapJodaInstantToTimestamp(exam.getDateStarted()))
            .addValue("customAccommodations", exam.isCustomAccommodations());

        String examEventInsertSQL = "INSERT INTO exam_event (\n" +
            "  exam_id,\n" +
            "  attempts, \n" +
            "  max_items, \n" +
            "  language_code, \n" +
            "  expire_from, \n" +
            "  browser_id, \n" +
            "  status,\n" +
            "  status_change_date, \n" +
            "  status_change_reason,\n" +
            "  date_changed,\n" +
            "  date_deleted,\n" +
            "  date_completed,\n" +
            "  date_scored,\n" +
            "  date_started,\n" +
            "  waiting_for_segment_approval,\n" +
            "  current_segment_position,\n" +
            "  abnormal_starts\n," +
            "  custom_accommodations\n" +
            ")\n" +
            "VALUES\n" +
            "(\n" +
            "  :examId,\n" +
            "  :attempts,\n" +
            "  :maxItems,\n" +
            "  :languageCode,\n" +
            "  :expireFrom, \n" +
            "  :browserId,\n" +
            "  :status,\n" +
            "  :statusChangeDate, \n" +
            "  :statusChangeReason,\n" +
            "  :dateChanged,\n" +
            "  :dateDeleted,\n" +
            "  :dateCompleted,\n" +
            "  :dateScored,\n" +
            "  :dateStarted,\n" +
            "  :waitingForSegmentApproval,\n" +
            "  :currentSegmentPosition,\n" +
            "  :abnormalStarts,\n" +
            "  :customAccommodations\n" +
            ");";

        int insertCount = jdbcTemplate.update(examEventInsertSQL, examEventParameters);

        if (insertCount != 1) {
            throw new CreateRecordException("Failed to insert exam event");
        }
    }
}
