package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

import tds.common.data.mapping.ResultSetMapperUtility;
import tds.common.data.mysql.UuidAdapter;
import tds.exam.models.FieldTestItemGroup;
import tds.exam.repositories.FieldTestItemGroupQueryRepository;

@Repository
public class FieldTestItemGroupQueryRepositoryImpl implements FieldTestItemGroupQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public FieldTestItemGroupQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") NamedParameterJdbcTemplate queryJdbcTemplate) {
        this.jdbcTemplate = queryJdbcTemplate;
    }

    @Override
    public List<FieldTestItemGroup> find(UUID examId, String segmentKey) {
        final SqlParameterSource parameters = new MapSqlParameterSource("examId", examId.toString())
            .addValue("segmentKey", segmentKey);

        final String SQL =
            "SELECT \n" +
                "   F.exam_id, \n" +
                "   F.session_id, \n" +
                "   F.segment_key, \n" +
                "   F.segment_id, \n" +
                "   F.position, \n" +
                "   F.language_code, \n" +
                "   F.item_count, \n" +
                "   F.group_id, \n" +
                "   F.group_key, \n" +
                "   F.block_id, \n" +
                "   F.created_at, \n" +
                "   FE.position_administered, \n" +
                "   FE.administered_at \n" +
                "FROM \n" +
                "   field_test_item_group F \n" +
                "JOIN ( \n" +
                "   SELECT \n" +
                "       field_test_item_group_id, \n" +
                "       MAX(id) AS id \n" +
                "   FROM \n" +
                "       field_test_item_group_event \n" +
                "   GROUP BY field_test_item_group_id \n" +
                ") last_event \n" +
                "   ON F.id = last_event.field_test_item_group_id \n" +
                "JOIN \n" +
                "   field_test_item_group_event FE \n" +
                "ON \n" +
                "   last_event.id = FE.id \n" +
                "WHERE \n" +
                "   F.exam_id = :examId AND \n" +
                "   F.segment_key = :segmentKey AND \n" +
                "   FE.deleted_at IS NULL \n" +
                "ORDER BY \n" +
                "   F.position \n";

        return jdbcTemplate.query(SQL, parameters, (rs, row) ->
            new FieldTestItemGroup.Builder()
                .withExamId(UUID.fromString(rs.getString("exam_id")))
                .withSessionId(UuidAdapter.getUUIDFromBytes(rs.getBytes("session_id")))
                .withSegmentKey(rs.getString("segment_key"))
                .withSegmentId(rs.getString("segment_id"))
                .withPosition(rs.getInt("position"))
                .withLanguageCode(rs.getString("language_code"))
                .withItemCount(rs.getInt("item_count"))
                .withGroupId(rs.getString("group_id"))
                .withGroupKey(rs.getString("group_key"))
                .withBlockId(rs.getString("block_id"))
                .withCreatedAt(ResultSetMapperUtility.mapTimestampToInstant(rs, "created_at"))
                .withPositionAdministered((Integer) rs.getObject("position_administered"))
                .withAdministeredAt(ResultSetMapperUtility.mapTimestampToInstant(rs, "administered_at"))
                .build());
    }
}
