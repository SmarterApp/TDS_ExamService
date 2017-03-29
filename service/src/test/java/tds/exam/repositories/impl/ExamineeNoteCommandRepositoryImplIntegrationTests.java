package tds.exam.repositories.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamineeNote;
import tds.exam.ExamineeNoteContext;
import tds.exam.builder.ExamBuilder;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamineeNoteCommandRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamineeNoteCommandRepositoryImplIntegrationTests {
    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    private ExamCommandRepository examCommandRepository;
    private ExamineeNoteCommandRepository examineeNoteCommandRepository;

    private final Exam mockExam = new ExamBuilder().build();

    @Before
    public void setup() {
        examCommandRepository = new ExamCommandRepositoryImpl(jdbcTemplate);
        examineeNoteCommandRepository = new ExamineeNoteCommandRepositoryImpl(jdbcTemplate);
    }

    @Test
    public void shouldInsertAnExamineeNote() {
        ExamineeNote note = new ExamineeNote.Builder()
            .withExamId(mockExam.getId())
            .withContext(ExamineeNoteContext.ITEM)
            .withItemPosition(5)
            .withNote("exam item note")
            .build();

        examCommandRepository.insert(mockExam);

        examineeNoteCommandRepository.insert(note);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void shouldNotInsertAnExamineeNoteWhenTheExamIsNotPresent() {
        ExamineeNote note = new ExamineeNote.Builder()
            .withExamId(UUID.randomUUID())
            .withContext(ExamineeNoteContext.ITEM)
            .withItemPosition(5)
            .withNote("exam item note")
            .build();

        examineeNoteCommandRepository.insert(note);
    }
}
