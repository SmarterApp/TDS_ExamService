package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

import java.util.stream.Stream;

import tds.common.data.mapping.ResultSetMapperUtility;
import tds.exam.models.ExamItem;
import tds.exam.models.ExamItemResponse;
import tds.exam.repositories.ExamItemCommandRepository;

public class ExamItemCommandRepositoryImpl implements ExamItemCommandRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamItemCommandRepositoryImpl(@Qualifier("commandJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(ExamItem... examItems) {
        final SqlParameterSource[] batchParameters = Stream.of(examItems)
            .map(examItem -> new MapSqlParameterSource("itemKey", examItem.getItemKey())
                .addValue("examPageId", examItem.getExamPageId())
                .addValue("position", examItem.getPosition())
                .addValue("isSelected", examItem.isSelected())
                .addValue("isMarkedForReview", examItem.isMarkedForReview())
                .addValue("isFieldTest", examItem.isFieldTest()))
            .toArray(MapSqlParameterSource[]::new);

        final String SQL =
            "INSERT INTO exam_item ( \n" +
                "   item_key, \n" +
                "   exam_page_id, \n" +
                "   position, \n" +
                "   is_selected, \n" +
                "   is_marked_for_review, \n" +
                "   is_fieldtest) \n" +
                "VALUES( \n" +
                "   :itemKey, \n" +
                "   :examPageId, \n" +
                "   :position, \n" +
                "   :isSelected, \n" +
                "   :isMarkedForReview, \n" +
                "   :isFieldTest)";

        jdbcTemplate.batchUpdate(SQL, batchParameters);
    }

    @Override
    public void insertResponses(ExamItemResponse... responses) {
        final SqlParameterSource[] batchParameters = Stream.of(responses)
            .map(response -> new MapSqlParameterSource("examItemId", response.getExamItemId())
                .addValue("response", response.getResponse())
                .addValue("createdAt", ResultSetMapperUtility.mapJodaInstantToTimestamp(response.getCreatedAt())))
            .toArray(MapSqlParameterSource[]::new);

        final String SQL =
            "INSERT INTO exam_item_response ( \n" +
                "   exam_item_id, \n" +
                "   response, \n" +
                "   created_at) \n" +
                "VALUES ( \n" +
                "   :examItemId, \n" +
                "   :response, \n" +
                "   :createdAt)";

        jdbcTemplate.batchUpdate(SQL, batchParameters);
    }
}
