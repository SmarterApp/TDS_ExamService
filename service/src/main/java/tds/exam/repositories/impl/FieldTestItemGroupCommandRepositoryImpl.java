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
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.sql.Timestamp;
import java.util.List;
import java.util.stream.Stream;

import tds.common.data.mapping.ResultSetMapperUtility;
import tds.exam.models.FieldTestItemGroup;
import tds.exam.repositories.FieldTestItemGroupCommandRepository;

import static tds.common.data.mapping.ResultSetMapperUtility.mapJodaInstantToTimestamp;
import static tds.common.data.mysql.UuidAdapter.getBytesFromUUID;

@Repository
public class FieldTestItemGroupCommandRepositoryImpl implements FieldTestItemGroupCommandRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public FieldTestItemGroupCommandRepositoryImpl(@Qualifier("commandJdbcTemplate") final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(final List<FieldTestItemGroup> fieldTestItemGroups) {
        final Timestamp createdAt = mapJodaInstantToTimestamp(Instant.now());
        final String ftItemGroupSQL =
            "INSERT INTO field_test_item_group ( \n" +
                "   exam_id, \n" +
                "   position, \n" +
                "   item_count, \n" +
                "   segment_id, \n" +
                "   segment_key, \n" +
                "   group_id, \n" +
                "   group_key, \n" +
                "   block_id, \n" +
                "   session_id, \n" +
                "   language_code, \n" +
                "   created_at \n" +
                ") \n " +
                "VALUES ( \n" +
                "   :examId, \n" +
                "   :position, \n" +
                "   :itemCount, \n" +
                "   :segmentId, \n" +
                "   :segmentKey, \n" +
                "   :groupId, \n" +
                "   :groupKey, \n" +
                "   :blockId, \n" +
                "   :sessionId, \n" +
                "   :languageCode, \n" +
                "   :createdAt \n" +
                ")";

        fieldTestItemGroups.forEach(fieldTestItemGroup -> {
            SqlParameterSource parameterSources = new MapSqlParameterSource("examId", fieldTestItemGroup.getExamId().toString())
                .addValue("position", fieldTestItemGroup.getPosition())
                .addValue("itemCount", fieldTestItemGroup.getItemCount())
                .addValue("segmentId", fieldTestItemGroup.getSegmentId())
                .addValue("segmentKey", fieldTestItemGroup.getSegmentKey())
                .addValue("groupId", fieldTestItemGroup.getGroupId())
                .addValue("groupKey", fieldTestItemGroup.getGroupKey())
                .addValue("blockId", fieldTestItemGroup.getBlockId())
                .addValue("sessionId", getBytesFromUUID(fieldTestItemGroup.getSessionId()))
                .addValue("languageCode", fieldTestItemGroup.getLanguageCode())
                .addValue("createdAt", createdAt);

            KeyHolder keyHolder = new GeneratedKeyHolder();
            jdbcTemplate.update(ftItemGroupSQL, parameterSources, keyHolder);
            fieldTestItemGroup.setId(keyHolder.getKey().longValue());

            update(fieldTestItemGroup);
        });
    }

    @Override
    public void update(final FieldTestItemGroup... fieldTestItemGroups) {
        final Timestamp createdAt = mapJodaInstantToTimestamp(Instant.now());
        final SqlParameterSource[] parameterSources = Stream.of(fieldTestItemGroups)
            .map(fieldTestItemGroup -> new MapSqlParameterSource("id", fieldTestItemGroup.getId())
                .addValue("deletedAt", ResultSetMapperUtility.mapInstantToTimestamp(fieldTestItemGroup.getDeletedAt()))
                .addValue("positionAdministered", fieldTestItemGroup.getPositionAdministered())
                .addValue("administeredAt", ResultSetMapperUtility.mapInstantToTimestamp(fieldTestItemGroup.getAdministeredAt()))
                .addValue("createdAt", createdAt)
            ).toArray(SqlParameterSource[]::new);
        final String SQL =
            "INSERT INTO field_test_item_group_event ( \n" +
                "   field_test_item_group_id, \n" +
                "   deleted_at, \n" +
                "   position_administered, \n" +
                "   administered_at, \n" +
                "   created_at \n" +
                ") \n" +
                "VALUES ( \n" +
                "   :id, \n" +
                "   :deletedAt, \n" +
                "   :positionAdministered, \n" +
                "   :administeredAt, \n" +
                "   :createdAt \n" +
                ")";

        jdbcTemplate.batchUpdate(SQL, parameterSources);
    }
}
