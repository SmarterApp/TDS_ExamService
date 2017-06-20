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
import org.springframework.stereotype.Repository;

import tds.exam.ExamineeNote;
import tds.exam.repositories.ExamineeNoteCommandRepository;

import static tds.common.data.mapping.ResultSetMapperUtility.mapJodaInstantToTimestamp;

@Repository
public class ExamineeNoteCommandRepositoryImpl implements ExamineeNoteCommandRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamineeNoteCommandRepositoryImpl(@Qualifier("commandJdbcTemplate") final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void insert(final ExamineeNote examineeNote) {
        final SqlParameterSource parameters = new MapSqlParameterSource("examId", examineeNote.getExamId().toString())
            .addValue("context", examineeNote.getContext().toString())
            .addValue("itemPosition", examineeNote.getItemPosition())
            .addValue("note", examineeNote.getNote())
            .addValue("createdAt", mapJodaInstantToTimestamp(Instant.now()));

        final String SQL =
            "INSERT INTO examinee_note( \n" +
                "   exam_id, \n" +
                "   context, \n" +
                "   item_position, \n" +
                "   note, \n" +
                "   created_at) \n" +
                "VALUES( \n" +
                "   :examId, \n" +
                "   :context, \n" +
                "   :itemPosition, \n" +
                "   :note, \n" +
                "   :createdAt)";

        jdbcTemplate.update(SQL, parameters);
    }
}
