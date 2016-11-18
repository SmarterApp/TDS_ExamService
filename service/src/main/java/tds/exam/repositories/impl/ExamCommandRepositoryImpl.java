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
        SqlParameterSource parameters = new MapSqlParameterSource("examId", getBytesFromUUID(exam.getId()))
            .addValue("clientName", exam.getClientName())
            .addValue("studentId", exam.getStudentId())
            .addValue("sessionId", getBytesFromUUID(exam.getSessionId()))
            .addValue("assessmentId", exam.getAssessmentId())
            .addValue("attempts", exam.getAttempts())
            .addValue("status", exam.getStatus().getStatus())
            .addValue("subject", exam.getSubject())
            .addValue("studentKey", exam.getStudentKey())
            .addValue("studentName", exam.getStudentName())
            .addValue("browserId", getBytesFromUUID(exam.getBrowserId()))
            .addValue("dateChanged", exam.getDateChanged())
            .addValue("assessmentWindowId", exam.getAssessmentWindowId())
            .addValue("segmented", exam.isSegmented())
            .addValue("assessmentAlgorithm", exam.getAssessmentAlgorithm())
            .addValue("assessmentKey", exam.getAssessmentKey())
            .addValue("environment", exam.getEnvironment())
            .addValue("dateJoined", mapJodaInstantToTimestamp(exam.getDateJoined()));


        String SQL = "INSERT INTO exam\n" +
            "(\n" +
            "exam_id,\n" +
            "client_name,\n" +
            "student_id,\n" +
            "session_id,\n" +
            "assessment_id,\n" +
            "attempts,\n" +
            "status,\n" +
            "subject,\n" +
            "student_key,\n" +
            "student_name,\n" +
            "browser_id,\n" +
            "date_changed,\n" +
            "assessment_window_id,\n" +
            "segmented,\n" +
            "assessment_algorithm,\n" +
            "assessment_key,\n" +
            "environment,\n" +
            "date_joined\n" +
            ") " +
            "VALUES (" +
            ":examId, " +
            ":clientName, " +
            ":studentId, " +
            ":sessionId, " +
            ":assessmentId, " +
            ":attempts, " +
            ":status, " +
            ":subject, " +
            ":studentKey, " +
            ":studentName, " +
            ":browserId, " +
            ":dateChanged, " +
            ":assessmentWindowId, " +
            ":segmented, " +
            ":assessmentAlgorithm, " +
            ":assessmentKey, " +
            ":environment, " +
            ":dateJoined " +
            ")";


        int insertCount = jdbcTemplate.update(SQL, parameters);

        if (insertCount != 1) {
            throw new CreateRecordException("Failed to insert exam");
        }
    }
}
