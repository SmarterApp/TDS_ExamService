package tds.exam.repositories.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamineeNote;
import tds.exam.ExamineeNoteContext;
import tds.exam.builder.ExamBuilder;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamineeNoteCommandRepository;
import tds.exam.repositories.ExamineeNoteQueryRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamineeNoteQueryRepositoryImplIntegrationTests {
    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    private ExamineeNoteCommandRepository examineeNoteCommandRepository;
    private ExamineeNoteQueryRepository examineeNoteQueryRepository;

    private final Exam mockExam = new ExamBuilder().build();

    @Before
    public void setup() {
        ExamCommandRepository examCommandRepository = new ExamCommandRepositoryImpl(jdbcTemplate);
        examineeNoteCommandRepository = new ExamineeNoteCommandRepositoryImpl(jdbcTemplate);
        examineeNoteQueryRepository = new ExamineeNoteQueryRepositoryImpl(jdbcTemplate);

        examCommandRepository.insert(mockExam);
    }

    @Test
    public void shouldFindAnExamineeNoteForAnExam() {
        ExamineeNote note = new ExamineeNote.Builder()
            .withExamId(mockExam.getId())
            .withContext(ExamineeNoteContext.EXAM)
            .withNote("exam note")
            .build();

        examineeNoteCommandRepository.insert(note);

        Optional<ExamineeNote> maybeResult = examineeNoteQueryRepository.findNoteInExamContext(mockExam.getId());

        assertThat(maybeResult).isPresent();

        ExamineeNote result = maybeResult.get();
        assertThat(result.getId()).isGreaterThan(0L);
        assertThat(result).isEqualToComparingOnlyGivenFields(note,
            "examId",
            "context",
            "itemPosition",
            "note");
    }

    @Test
    public void shouldOnlyReturnExamScopedNoteWhenExamHasMultipleNotes() {
        ExamineeNote examNote = new ExamineeNote.Builder()
            .withExamId(mockExam.getId())
            .withContext(ExamineeNoteContext.EXAM)
            .withNote("exam note")
            .build();

        ExamineeNote itemNote = new ExamineeNote.Builder()
            .withExamId(mockExam.getId())
            .withContext(ExamineeNoteContext.ITEM)
            .withItemPosition(5)
            .withNote("exam item note")
            .build();

        examineeNoteCommandRepository.insert(examNote);
        examineeNoteCommandRepository.insert(itemNote);

        Optional<ExamineeNote> maybeResult = examineeNoteQueryRepository.findNoteInExamContext(mockExam.getId());

        assertThat(maybeResult).isPresent();

        ExamineeNote examNoteResult = maybeResult.get();
        assertThat(examNoteResult.getId()).isGreaterThan(0L);
        assertThat(examNoteResult).isEqualToComparingOnlyGivenFields(examNote,
            "examId",
            "context",
            "itemPosition",
            "note");
    }

    @Test
    public void shouldReturnOptionalEmptyWhenExamOnlyHasItemScopedNotes() {
        ExamineeNote note = new ExamineeNote.Builder()
            .withExamId(mockExam.getId())
            .withContext(ExamineeNoteContext.ITEM)
            .withItemPosition(5)
            .withNote("exam item note")
            .build();

        examineeNoteCommandRepository.insert(note);

        Optional<ExamineeNote> maybeResult = examineeNoteQueryRepository.findNoteInExamContext(mockExam.getId());

        assertThat(maybeResult).isNotPresent();
    }

    @Test
    public void shouldReturnOptionalEmptyWhenExamDoesNotHaveAnyNotes() {
        Optional<ExamineeNote> maybeResult = examineeNoteQueryRepository.findNoteInExamContext(UUID.randomUUID());

        assertThat(maybeResult).isNotPresent();
    }
}
