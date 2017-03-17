package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamineeNote;
import tds.exam.ExamineeNoteContext;
import tds.exam.repositories.ExamineeNoteQueryRepository;

@Repository
public class ExamineeNoteQueryRepositoryImpl implements ExamineeNoteQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Autowired
    public ExamineeNoteQueryRepositoryImpl(@Qualifier("queryJdbcTemplate") final NamedParameterJdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<ExamineeNote> findNoteInExamContext(final UUID examId) {
        final SqlParameterSource parameters = new MapSqlParameterSource("examId", examId.toString())
            .addValue("context", ExamineeNoteContext.EXAM.toString());
        final String SQL =
            "SELECT \n" +
                "   id, \n" +
                "   exam_id, \n" +
                "   context, \n" +
                "   item_position, \n" +
                "   note \n" +
                "FROM \n" +
                "   examinee_note \n" +
                "WHERE \n" +
                "   exam_id = :examId \n" +
                "   AND context = :context \n" +
                "ORDER BY \n" +
                "   id DESC \n" +
                "LIMIT 1";

        Optional<ExamineeNote> maybeExamineeNote;
        try {
            maybeExamineeNote = Optional.of(jdbcTemplate.queryForObject(SQL, parameters, (rs, r) ->
                new ExamineeNote.Builder()
                    .withId(rs.getLong("id"))
                    .withExamId(UUID.fromString(rs.getString("exam_id")))
                    .withContext(ExamineeNoteContext.fromType(rs.getString("context")))
                    .withItemPosition(rs.getInt("item_position"))
                    .withNote(rs.getString("note"))
                    .build()));
        } catch (EmptyResultDataAccessException e) {
            maybeExamineeNote = Optional.empty();
        }

        return maybeExamineeNote;
    }
}
