package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;

import tds.common.data.mapping.ResultSetMapperUtility;
import tds.exam.models.FieldTestItemGroup;
import tds.exam.repositories.FieldTestItemGroupCommandRepository;

import static tds.common.data.mysql.UuidAdapter.getBytesFromUUID;

@Repository
public class FieldTestItemGroupCommandRepositoryImpl implements FieldTestItemGroupCommandRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public FieldTestItemGroupCommandRepositoryImpl(@Qualifier("commandJdbcTemplate") NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(List<FieldTestItemGroup> fieldTestItemGroups) {
        final String ftItemGroupSQL =
            "INSERT INTO field_test_item_group ( \n" +
                "   exam_id, \n" +
                "   position, \n" +
                "   num_items, \n" +
                "   segment_id, \n" +
                "   segment_key, \n" +
                "   group_id, \n" +
                "   group_key, \n" +
                "   block_id, \n" +
                "   session_id, \n" +
                "   language_code \n" +
                ") \n " +
                "VALUES ( \n" +
                "   :examId, \n" +
                "   :position, \n" +
                "   :numItems, \n" +
                "   :segmentId, \n" +
                "   :segmentKey, \n" +
                "   :groupId, \n" +
                "   :groupKey, \n" +
                "   :blockId, \n" +
                "   :sessionId, \n" +
                "   :languageCode \n" +
                ")";

        fieldTestItemGroups.forEach(fieldTestItemGroup -> {
            SqlParameterSource parameterSources = new MapSqlParameterSource("examId", getBytesFromUUID(fieldTestItemGroup.getExamId()))
                .addValue("position", fieldTestItemGroup.getPosition())
                .addValue("numItems", fieldTestItemGroup.getNumItems())
                .addValue("segmentId", fieldTestItemGroup.getSegmentId())
                .addValue("segmentKey", fieldTestItemGroup.getSegmentKey())
                .addValue("groupId", fieldTestItemGroup.getGroupId())
                .addValue("groupKey", fieldTestItemGroup.getGroupKey())
                .addValue("blockId", fieldTestItemGroup.getBlockId())
                .addValue("sessionId", getBytesFromUUID(fieldTestItemGroup.getSessionId()))
                .addValue("languageCode", fieldTestItemGroup.getLanguageCode());

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(ftItemGroupSQL, parameterSources, keyHolder);
            fieldTestItemGroup.setId(keyHolder.getKey().longValue());

            update(fieldTestItemGroup);
        });
    }

    private void update(FieldTestItemGroup fieldTestItemGroup) {
        final SqlParameterSource params = new MapSqlParameterSource("id", fieldTestItemGroup.getId())
            .addValue("deletedAt", ResultSetMapperUtility.mapInstantToTimestamp(fieldTestItemGroup.getDeletedAt()))
            .addValue("positionAdministered", fieldTestItemGroup.getPositionAdministered())
            .addValue("administeredAt", ResultSetMapperUtility.mapInstantToTimestamp(fieldTestItemGroup.getAdministeredAt()));

        final String updateSQL =
            "INSERT INTO field_test_item_group_event ( \n" +
                "   field_test_item_group_id, \n" +
                "   deleted_at, \n" +
                "   position_administered, \n" +
                "   administered_at \n" +
                ") \n" +
                "VALUES ( \n" +
                "   :id, \n" +
                "   :deletedAt, \n" +
                "   :positionAdministered, \n" +
                "   :administeredAt \n" +
                ")";

        jdbcTemplate.update(updateSQL, params);
    }
}
