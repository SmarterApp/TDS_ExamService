package tds.exam.web.endpoints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamineeNote;
import tds.exam.ExamineeNoteContext;
import tds.exam.services.ExamineeNoteService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamineeNoteControllerTest {
    private ExamineeNoteController controller;

    @Mock
    private ExamineeNoteService mockExamineeNoteService;

    @Before
    public void setUp() {
        HttpServletRequest request = new MockHttpServletRequest();
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(requestAttributes);

        controller = new ExamineeNoteController(mockExamineeNoteService);
    }

    @Test
    public void shouldReturnAnExamineeNote() {
        ExamineeNote examineeNote = new ExamineeNote.Builder()
            .withId(42L)
            .withExamId(UUID.randomUUID())
            .withContext(ExamineeNoteContext.EXAM)
            .withNote("Exam note")
            .build();
        when(mockExamineeNoteService.findNoteInExamContext(any(UUID.class)))
            .thenReturn(Optional.of(examineeNote));

        ResponseEntity<ExamineeNote> result = controller.getNoteInExamContext(UUID.randomUUID());
        verify(mockExamineeNoteService).findNoteInExamContext(any(UUID.class));

        assertThat(result.getBody().getId()).isEqualTo(examineeNote.getId());
        assertThat(result.getBody().getExamId()).isEqualTo(examineeNote.getExamId());
        assertThat(result.getBody().getContext()).isEqualTo(examineeNote.getContext());
        assertThat(result.getBody().getItemPosition()).isEqualTo(examineeNote.getItemPosition());
        assertThat(result.getBody().getNote()).isEqualTo(examineeNote.getNote());
    }

    @Test
    public void shouldReturnNotFoundWhenExamHasNoExamineeNote() {
        when(mockExamineeNoteService.findNoteInExamContext(any(UUID.class)))
            .thenReturn(Optional.empty());

        ResponseEntity<ExamineeNote> result = controller.getNoteInExamContext(UUID.randomUUID());
        verify(mockExamineeNoteService).findNoteInExamContext(any(UUID.class));

        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    public void shouldInsertANewExamineeNote() {
        ExamineeNote examineeNote = new ExamineeNote.Builder()
            .withId(42L)
            .withExamId(UUID.randomUUID())
            .withContext(ExamineeNoteContext.EXAM)
            .withNote("Exam note")
            .build();
        doNothing().when(mockExamineeNoteService).insert(any(ExamineeNote.class));

        controller.insert(UUID.randomUUID(), examineeNote);
        verify(mockExamineeNoteService).insert(any(ExamineeNote.class));
    }
}
