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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.common.data.mapping.ResultSetMapperUtility;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamItemResponseScore;
import tds.exam.ExamPage;
import tds.exam.ExamScoringStatus;
import tds.exam.repositories.ExamPageWrapperQueryRepository;
import tds.exam.wrapper.ExamPageWrapper;

@Repository
public class ExamPageWrapperQueryRepositoryImpl implements ExamPageWrapperQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final ExamPageResultSetExtractor examPageResultExtractor = new ExamPageResultSetExtractor();

    private static final String EXAM_PAGE_WRAPPER_WITH_ITEM_SELECT = "SELECT \n" +
        "   page.id AS page_id, \n" +
        "   page.page_position, \n" +
        "   page.item_group_key, \n" +
        "   page.group_items_required, \n" +
        "   page.exam_id, \n" +
        "   page.created_at, \n" +
        "   page.segment_key, \n" +
        "   page_event.started_at, \n" +
        "   page_event.page_duration, \n" +
        "   page_event.visible, \n" +
        "   item.id AS item_id, \n" +
        "   item.item_key, \n" +
        "   item.assessment_item_bank_key, \n" +
        "   item.assessment_item_key, \n" +
        "   item.item_type, \n" +
        "   item.exam_page_id, \n" +
        "   item.position AS item_position, \n" +
        "   item.is_fieldtest, \n" +
        "   item.is_required, \n" +
        "   item.item_file_path, \n" +
        "   item.stimulus_file_path, \n" +
        "   item.created_at, \n" +
        "   item.group_id, \n" +
        "   response.response, \n" +
        "   response.sequence, \n" +
        "   response.is_valid, \n" +
        "   response.is_selected, \n" +
        "   response.score, \n" +
        "   response.scoring_status, \n" +
        "   response.scoring_rationale, \n" +
        "   response.scoring_dimensions, \n" +
        "   response.created_at AS response_created_at, \n" +
        "   response.scored_at, \n" +
        "   response.is_marked_for_review, \n" +
        "   segment.segment_id, \n" +
        "   segment.segment_position \n" +
        "FROM \n" +
        "   exam_page page \n" +
        "JOIN ( \n" +
        "   SELECT \n" +
        "       MAX(id) AS id, \n" +
        "       exam_page_id \n" +
        "   FROM \n" +
        "       exam_page_event \n" +
        "   WHERE \n" +
        "       exam_id = :examId \n" +
        "   GROUP BY \n" +
        "       exam_page_id) last_page_event \n" +
        "   ON page.id = last_page_event.exam_page_id \n" +
        "JOIN \n" +
        "   exam_page_event AS page_event \n" +
        "   ON page_event.id = last_page_event.id \n" +
        "JOIN \n" +
        "   exam_segment segment \n" +
        "   ON segment.exam_id = page.exam_id \n" +
        "   AND segment.segment_key = page.segment_key \n" +
        "JOIN \n" +
        "   exam_item item \n" +
        "   ON page.id = item.exam_page_id \n" +
        "LEFT JOIN (" +
        "   SELECT \n" +
        "       MAX(id) AS id, \n" +
        "       exam_item_id \n" +
        "   FROM \n" +
        "       exam_item_response \n" +
        "   WHERE \n" +
        "       exam_id = :examId \n" +
        "   GROUP BY \n" +
        "       exam_item_id) most_recent_response \n" +
        "   ON item.id = most_recent_response.exam_item_id \n" +
        "LEFT JOIN \n" +
        "   exam_item_response response \n" +
        "   ON most_recent_response.id = response.id \n";

    @Autowired
    public ExamPageWrapperQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ExamPageWrapper> findPageWithItems(final UUID examId, final int position) {
        final MapSqlParameterSource parameters = new MapSqlParameterSource("examId", examId.toString())
            .addValue("position", position);

        final String SQL =
            EXAM_PAGE_WRAPPER_WITH_ITEM_SELECT +
                "WHERE \n" +
                "   page.exam_id = :examId \n" +
                "   AND page.page_position = :position";

        List<ExamPageWrapper> examPageWrappers = jdbcTemplate.query(SQL, parameters, examPageResultExtractor);

        if (examPageWrappers.isEmpty()) {
            return Optional.empty();
        }

        return Optional.of(examPageWrappers.get(0));
    }

    @Override
    public List<ExamPageWrapper> findPagesWithItems(final UUID examId) {
        final MapSqlParameterSource parameters = new MapSqlParameterSource("examId", examId.toString());

        final String SQL =
            EXAM_PAGE_WRAPPER_WITH_ITEM_SELECT +
                "WHERE \n" +
                "   page.exam_id = :examId";

        return jdbcTemplate.query(SQL, parameters, examPageResultExtractor);
    }

    @Override
    public List<ExamPageWrapper> findPagesForExamSegment(final UUID examId, final String segmentKey) {
        final MapSqlParameterSource parameters = new MapSqlParameterSource("examId", examId.toString())
            .addValue("segmentKey", segmentKey);

        final String SQL =
            EXAM_PAGE_WRAPPER_WITH_ITEM_SELECT +
                "WHERE \n" +
                "   page.exam_id = :examId \n" +
                "   AND page.segment_key = :segmentKey";

        return jdbcTemplate.query(SQL, parameters, examPageResultExtractor);
    }

    private static class ExamPageResultSetExtractor implements ResultSetExtractor<List<ExamPageWrapper>> {
        @Override
        public List<ExamPageWrapper> extractData(ResultSet resultExtractor) throws SQLException, DataAccessException {
            Map<UUID, List<ExamItem>> itemsForPage = new HashMap<>();
            Map<UUID, ExamPage> examPages = new HashMap<>();

            while (resultExtractor.next()) {
                UUID pageId = UUID.fromString(resultExtractor.getString("page_id"));
                if (!examPages.containsKey(pageId)) {
                    ExamPage page = new ExamPage.Builder()
                        .withId(UUID.fromString(resultExtractor.getString("page_id")))
                        .withPagePosition(resultExtractor.getInt("page_position"))
                        .withSegmentKey(resultExtractor.getString("segment_key"))
                        .withDuration(resultExtractor.getLong("page_duration"))
                        .withItemGroupKey(resultExtractor.getString("item_group_key"))
                        .withGroupItemsRequired(resultExtractor.getInt("group_items_required"))
                        .withExamId(UUID.fromString(resultExtractor.getString("exam_id")))
                        .withVisible(resultExtractor.getBoolean("visible"))
                        .withCreatedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(resultExtractor, "created_at"))
                        .withStartedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(resultExtractor, "started_at"))
                        .build();

                    examPages.put(pageId, page);
                }

                // An item might not have a response
                ExamItemResponse response = null;
                if (resultExtractor.getString("response") != null) {
                    response = new ExamItemResponse.Builder()
                        .withExamItemId(UUID.fromString(resultExtractor.getString("item_id")))
                        .withExamId(UUID.fromString(resultExtractor.getString("exam_id")))
                        .withResponse(resultExtractor.getString("response"))
                        .withSequence(resultExtractor.getInt("sequence"))
                        .withValid(resultExtractor.getBoolean("is_valid"))
                        .withSelected(resultExtractor.getBoolean("is_selected"))
                        .withMarkedForReview(resultExtractor.getBoolean("is_marked_for_review"))
                        .withScore(new ExamItemResponseScore.Builder()
                            .withScore(resultExtractor.getInt("score"))
                            .withScoringStatus(resultExtractor.getString("scoring_status") != null
                                ? ExamScoringStatus.fromType(resultExtractor.getString("scoring_status"))
                                : null)
                            .withScoringRationale(resultExtractor.getString("scoring_rationale"))
                            .withScoringDimensions(resultExtractor.getString("scoring_dimensions"))
                            .withScoredAt(ResultSetMapperUtility.mapTimestampToJodaInstant(resultExtractor, "scored_at"))
                            .build())
                        .withCreatedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(resultExtractor, "response_created_at"))
                        .build();
                }

                if (!itemsForPage.containsKey(pageId)) {
                    itemsForPage.put(pageId, new ArrayList<>());
                }

                List<ExamItem> items = itemsForPage.get(pageId);

                items.add(new ExamItem.Builder(UUID.fromString(resultExtractor.getString("item_id")))
                    .withItemKey(resultExtractor.getString("item_key"))
                    .withAssessmentItemBankKey(resultExtractor.getLong("assessment_item_bank_key"))
                    .withAssessmentItemKey(resultExtractor.getLong("assessment_item_key"))
                    .withItemType(resultExtractor.getString("item_type"))
                    .withExamPageId(UUID.fromString(resultExtractor.getString("exam_page_id")))
                    .withPosition(resultExtractor.getInt("item_position"))
                    .withFieldTest(resultExtractor.getBoolean("is_fieldtest"))
                    .withRequired(resultExtractor.getBoolean("is_required"))
                    .withItemFilePath(resultExtractor.getString("item_file_path"))
                    .withStimulusFilePath(resultExtractor.getString("stimulus_file_path"))
                    .withResponse(response)
                    .withGroupId(resultExtractor.getString("group_id"))
                    .withCreatedAt(ResultSetMapperUtility.mapTimestampToJodaInstant(resultExtractor, "created_at"))
                    .build());
            }

            return examPages
                .values()
                .stream()
                .map(examPage -> {
                    List<ExamItem> items = itemsForPage.get(examPage.getId())
                        .stream()
                        .sorted(Comparator.comparingInt(ExamItem::getPosition))
                        .collect(Collectors.toList());

                    return new ExamPageWrapper(examPage, items);
                })
                .sorted(Comparator.comparingInt(o -> o.getExamPage().getPagePosition()))
                .collect(Collectors.toList());
        }
    }
}
