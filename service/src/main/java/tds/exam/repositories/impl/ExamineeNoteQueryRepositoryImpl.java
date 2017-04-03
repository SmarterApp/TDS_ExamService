package tds.exam.repositories.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamineeNote;
import tds.exam.ExamineeNoteContext;
import tds.exam.repositories.ExamineeNoteQueryRepository;

@Repository
public class ExamineeNoteQueryRepositoryImpl implements ExamineeNoteQueryRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private static final RowMapper<ExamineeNote> examineeNoteRowMapper = new ExamineeNoteRowMapper();

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
            maybeExamineeNote = Optional.of(jdbcTemplate.queryForObject(SQL, parameters, examineeNoteRowMapper));
        } catch (EmptyResultDataAccessException e) {
            maybeExamineeNote = Optional.empty();
        }

        return maybeExamineeNote;
    }

    @Override
    public List<ExamineeNote> findAllNotes(final UUID examId) {
        final SqlParameterSource parameters = new MapSqlParameterSource("examId", examId.toString());
        final String SQL =
            "SELECT \n" +
                "   note.id, \n" +
                "   note.exam_id, \n" +
                "   note.context, \n" +
                "   note.item_position, \n" +
                "   note.note \n" +
                "FROM \n" +
                "   examinee_note note \n" +
                "INNER JOIN ( \n" +
                "   SELECT \n" +
                "       MAX(id) AS id, \n" +
                "       exam_id, \n" +
                "       item_position \n" +
                "   FROM \n" +
                "       examinee_note \n" +
                "   GROUP BY \n" +
                "       exam_id, \n" +
                "       item_position \n" +
                ") last_note \n" +
                "   ON last_note.id = note.id \n" +
                "WHERE \n" +
                "   note.exam_id = :examId";

        return jdbcTemplate.query(SQL, parameters, examineeNoteRowMapper);
    }

    private static class ExamineeNoteRowMapper implements RowMapper<ExamineeNote> {
        @Override
        public ExamineeNote mapRow(final ResultSet rs, final int rowNum) throws SQLException {
            return new ExamineeNote.Builder()
                .withId(rs.getLong("id"))
                .withExamId(UUID.fromString(rs.getString("exam_id")))
                .withContext(ExamineeNoteContext.fromType(rs.getString("context")))
                .withItemPosition(rs.getInt("item_position"))
                .withNote(rs.getString("note"))
                .build();
        }
    }
}
