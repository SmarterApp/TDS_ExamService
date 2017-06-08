package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamineeNote;
import tds.exam.ExamineeNoteContext;
import tds.exam.repositories.ExamineeNoteCommandRepository;
import tds.exam.repositories.ExamineeNoteQueryRepository;
import tds.exam.services.ExamineeNoteService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamineeNoteServiceImplTest {
    @Mock
    private ExamineeNoteQueryRepository mockExamineeNoteQueryRepository;

    @Mock
    private ExamineeNoteCommandRepository mockExamineeNoteCommandRepository;

    private ExamineeNoteService examineeNoteService;

    @Before
    public void setup() {
        examineeNoteService = new ExamineeNoteServiceImpl(mockExamineeNoteCommandRepository,
            mockExamineeNoteQueryRepository);
    }

    @Test
    public void shouldGetAnExamineeNote() {
        ExamineeNote note = new ExamineeNote.Builder()
            .withId(42L)
            .withExamId(UUID.randomUUID())
            .withContext(ExamineeNoteContext.EXAM)
            .withNote("exam note")
            .build();

        when(mockExamineeNoteQueryRepository.findNoteInExamContext(any(UUID.class)))
            .thenReturn(Optional.of(note));

        Optional<ExamineeNote> result = examineeNoteService.findNoteInExamContext(UUID.randomUUID());
        verify(mockExamineeNoteQueryRepository).findNoteInExamContext(any(UUID.class));

        assertThat(result).isPresent();
    }

    @Test
    public void shouldGetOptionalEmptyForAnExamThatHasNoExamineeNoteWithExamScope() {
        when(mockExamineeNoteQueryRepository.findNoteInExamContext(any(UUID.class)))
            .thenReturn(Optional.empty());

        Optional<ExamineeNote> result = examineeNoteService.findNoteInExamContext(UUID.randomUUID());
        verify(mockExamineeNoteQueryRepository).findNoteInExamContext(any(UUID.class));

        assertThat(result).isNotPresent();
    }

    @Test
    public void shouldInsertANewExamineeNote() {
        ExamineeNote note = new ExamineeNote.Builder()
            .withExamId(UUID.randomUUID())
            .withContext(ExamineeNoteContext.ITEM)
            .withItemPosition(5)
            .withNote("exam note")
            .build();

        doNothing().when(mockExamineeNoteCommandRepository).insert(any(ExamineeNote.class));

        examineeNoteService.insert(note);
        verify(mockExamineeNoteCommandRepository).insert(any(ExamineeNote.class));
    }
}
