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
    public void save(Exam exam) {
        SqlParameterSource examParameters = new MapSqlParameterSource("id", getBytesFromUUID(exam.getId()))
            .addValue("clientName", exam.getClientName())
            .addValue("environment", exam.getEnvironment())
            .addValue("sessionId", getBytesFromUUID(exam.getSessionId()))
            .addValue("browserId", getBytesFromUUID(exam.getBrowserId()))
            .addValue("subject", exam.getSubject())
            .addValue("loginSsid", exam.getLoginSSID())
            .addValue("studentId", exam.getStudentId())
            .addValue("studentName", exam.getStudentName())
            .addValue("assessmentId", exam.getAssessmentId())
            .addValue("assessmentKey", exam.getAssessmentKey())
            .addValue("assessmentWindowId", exam.getAssessmentWindowId())
            .addValue("assessmentAlgorithm", exam.getAssessmentAlgorithm())
            .addValue("segmented", exam.isSegmented())
            .addValue("dateStarted", mapJodaInstantToTimestamp(exam.getDateStarted()));

        String examInsertSQL = "INSERT INTO exam \n" +
            "(\n" +
            "  id,\n" +
            "  client_name, \n" +
            "  environment,\n" +
            "  session_id,\n" +
            "  browser_id,\n" +
            "  subject,\n" +
            "  login_ssid,\n" +
            "  student_id,\n" +
            "  student_name,\n" +
            "  assessment_id,\n" +
            "  assessment_key,\n" +
            "  assessment_window_id,\n" +
            "  assessment_algorithm,\n" +
            "  segmented,\n" +
            "  date_started \n" +
            ")\n" +
            "VALUES\n" +
            "(\n" +
            "  :id,\n" +
            "  :clientName,\n" +
            "  :environment,\n" +
            "  :sessionId,\n" +
            "  :browserId,\n" +
            "  :subject,\n" +
            "  :loginSsid,\n" +
            "  :studentId,\n" +
            "  :studentName,\n" +
            "  :assessmentId,\n" +
            "  :assessmentKey,\n" +
            "  :assessmentWindowId,\n" +
            "  :assessmentAlgorithm,\n" +
            "  :segmented,\n" +
            "  :dateStarted\n" +
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
            .addValue("statusChangeReason", exam.getStatusChangeReason())
            .addValue("dateChanged", mapJodaInstantToTimestamp(exam.getDateChanged()))
            .addValue("dateDeleted", mapJodaInstantToTimestamp(exam.getDateDeleted()))
            .addValue("dateCompleted", mapJodaInstantToTimestamp(exam.getDateCompleted()))
            .addValue("dateScored", mapJodaInstantToTimestamp(exam.getDateScored()))
            .addValue("dateJoined", mapJodaInstantToTimestamp(exam.getDateJoined()));

        String examEventInsertSQL = "INSERT INTO exam_event (\n" +
            "  exam_id,\n" +
            "  attempts,\n" +
            "  status,\n" +
            "  status_change_reason,\n" +
            "  date_changed,\n" +
            "  date_deleted,\n" +
            "  date_completed,\n" +
            "  date_scored,\n" +
            "  date_joined\n" +
            ")\n" +
            "VALUES\n" +
            "(\n" +
            "  :examId,\n" +
            "  :attempts,\n" +
            "  :status,\n" +
            "  :statusChangeReason,\n" +
            "  :dateChanged,\n" +
            "  :dateDeleted,\n" +
            "  :dateCompleted,\n" +
            "  :dateScored,\n" +
            "  :dateJoined\n" +
            ");";

        int insertCount = jdbcTemplate.update(examEventInsertSQL, examEventParameters);

        if (insertCount != 1) {
            throw new CreateRecordException("Failed to insert exam event");
        }
    }
}
