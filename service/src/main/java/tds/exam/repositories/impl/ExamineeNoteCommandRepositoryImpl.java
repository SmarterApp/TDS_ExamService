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
