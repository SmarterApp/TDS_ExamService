package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

import tds.common.data.mapping.ResultSetMapperUtility;
import tds.common.data.mysql.UuidAdapter;
import tds.exam.models.FieldTestItemGroup;
import tds.exam.repositories.FieldTestItemGroupQueryRepository;

@Repository
public class FieldTestItemGroupQueryRepositoryImpl implements FieldTestItemGroupQueryRepository {
    private static final FieldTestItemGroupMapper fieldTestItemGroupMapper = new FieldTestItemGroupMapper();
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public FieldTestItemGroupQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") final NamedParameterJdbcTemplate queryJdbcTemplate) {
        this.jdbcTemplate = queryJdbcTemplate;
    }

    @Override
    public List<FieldTestItemGroup> find(final UUID examId, final String segmentKey) {
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
                "JOIN \n" +
                "   field_test_item_group_event FE \n" +
                "   ON FE.field_test_item_group_id = F.id \n" +
                "   AND FE.id = (SELECT MAX(id) \n" +
                "                FROM field_test_item_group_event \n" +
                "                WHERE field_test_item_group_id = F.id \n" +
                "                AND deleted_at IS NULL) \n" +
                "WHERE \n" +
                "   F.exam_id = :examId " +
                "   AND F.segment_key = :segmentKey \n" +
                "ORDER BY \n" +
                "   F.position";

        return jdbcTemplate.query(SQL, parameters, fieldTestItemGroupMapper);
    }

    @Override
    public List<FieldTestItemGroup> findUsageInExam(final UUID examId) {
        // CommonDLL#_OnStatus_Completed_SP, lines 1445 - 1453: Find all the field test items that were administered
        // during an exam.  In this case, we only need an "abbreviated" representation of the FieldTestItemGroup, one
        // that shows the first position when then field test item was viewed by the student.  This information is then
        // used to update the field test item usage (when it was administered and what position it was administered in)
        // for this exam.
        // NOTE:  The block_id is omitted from this query; it is only used in the SELECT statement of the legacy query
        // and never appears to be updated.
        // NOTE:  The SELECT statement below returns field test items regardless whether their "deleted_at" column is
        // set (that is, deleted and non-deleted field test items are returned).  This is because the legacy query does
        // not filter deleted field test items (CommonDLL#_OnStatus_Completed_SP, line 1445), even though the
        // session.ft_opportunityitem table has a "deleted" column.  CommonDLL#_OnStatus_Completed_SP updates records in
        // session.ft_opportunityitem regardless of whether they are marked as deleted.
        final SqlParameterSource parameters = new MapSqlParameterSource("examId", examId.toString());
        final String SQL =
            "USE exam;\n" +
                "SELECT \n" +
                "   ftitem_group.id, \n" +
                "   ftitem_group.exam_id, \n" +
                "   ftitem_group.language_code, \n" +
                "   page.item_group_key, \n" +
                "   segment.segment_key, \n" +
                "   MIN(item.position) AS position_administered, \n" +
                "   ftitem_event.deleted_at \n" +
                "FROM \n" +
                "   exam_page page \n" +
                "JOIN \n" +
                "   exam_segment segment \n" +
                "   ON segment.exam_id = page.exam_id \n" +
                "   AND segment.segment_key = page.segment_key \n" +
                "JOIN \n" +
                "   exam_item item \n" +
                "   ON page.id = item.exam_page_id \n" +
                "JOIN \n" +
                "   field_test_item_group ftitem_group \n" +
                "   ON page.exam_id = ftitem_group.exam_id \n" +
                "   AND segment.segment_key = ftitem_group.segment_key \n" +
                "   AND page.item_group_key = ftitem_group.group_key \n" +
                "JOIN \n" +
                "   field_test_item_group_event ftitem_event \n" +
                "   ON ftitem_event.field_test_item_group_id = ftitem_group.id \n" +
                "   AND ftitem_event.id = (SELECT MAX(id) \n" +
                "                          FROM field_test_item_group_event \n" +
                "                          WHERE field_test_item_group_id = ftitem_group.id) \n" +
                "WHERE \n" +
                "   page.exam_id =  :examId \n" +
                "   AND item.is_fieldtest = 1 \n" +
                "GROUP BY \n" +
                "   ftitem_group.id, \n" +
                "   ftitem_group.exam_id, \n" +
                "   ftitem_group.language_code, \n" +
                "   page.item_group_key, \n" +
                "   segment.segment_key, \n" +
                "   ftitem_event.deleted_at";

        return jdbcTemplate.query(SQL, parameters, (rs, r) -> new FieldTestItemGroup.Builder()
            .withId(rs.getLong("id"))
            .withExamId(UUID.fromString(rs.getString("exam_id")))
            .withLanguageCode(rs.getString("language_code"))
            .withGroupKey(rs.getString("item_group_key"))
            .withSegmentKey(rs.getString("segment_key"))
            .withPositionAdministered(rs.getInt("position_administered"))
            .withDeletedAt(ResultSetMapperUtility.mapTimestampToInstant(rs, "deleted_at"))
            .build());
    }

    private static class FieldTestItemGroupMapper implements RowMapper<FieldTestItemGroup> {
        @Override
        public FieldTestItemGroup mapRow(ResultSet rs, int i) throws SQLException {
            return new FieldTestItemGroup.Builder()
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
                .build();
        }
    }
}
