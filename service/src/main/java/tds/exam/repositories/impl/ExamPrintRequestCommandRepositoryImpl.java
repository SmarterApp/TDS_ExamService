package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.UUID;

import tds.exam.ExamPrintRequest;
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
            .addValue("description", examPrintRequest.getDescription());

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
                "   description \n" +
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
                "   :description \n" +
                ")";

        jdbcTemplate.update(examPrintRequestSQL, params);
        update(examPrintRequest);
    }

    @Override
    public void update(final ExamPrintRequest examPrintRequest) {
        final SqlParameterSource params = new MapSqlParameterSource("examRequestId", examPrintRequest.getId().toString())
            .addValue("approvedAt", mapJodaInstantToTimestamp(examPrintRequest.getApprovedAt()))
            .addValue("deniedAt", mapJodaInstantToTimestamp(examPrintRequest.getDeniedAt()))
            .addValue("reasonDenied", examPrintRequest.getReasonDenied());

        final String updateExamPrintRequestSQL =
            "INSERT INTO \n" +
                "exam.exam_print_request_event ( \n" +
                "   exam_print_request_id, \n" +
                "   approved_at, \n" +
                "   denied_at, \n" +
                "   reason_denied \n" +
                ") \n" +
                "VALUES ( \n" +
                "   :examRequestId, \n" +
                "   :approvedAt, \n" +
                "   :deniedAt, \n" +
                "   :reasonDenied \n" +
                ")";

        jdbcTemplate.update(updateExamPrintRequestSQL, params);
    }
}
