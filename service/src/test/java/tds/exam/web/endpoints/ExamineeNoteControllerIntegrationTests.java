package tds.exam.web.endpoints;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamInfo;
import tds.exam.ExamineeNote;
import tds.exam.ExamineeNoteContext;
import tds.exam.WebMvcControllerIntegrationTest;
import tds.exam.services.ExamineeNoteService;

import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcControllerIntegrationTest(controllers = ExamineeNoteController.class)
public class ExamineeNoteControllerIntegrationTests {
    @Autowired
    private MockMvc http;

    @MockBean
    private ExamineeNoteService mockExamineeNoteService;

    @Test
    public void shouldGetAnExamineeNote() throws Exception {
        ExamInfo examInfo = new ExamInfo(UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID());
        ExamineeNote examineeNote = new ExamineeNote.Builder()
            .withId(42L)
            .withExamId(examInfo.getExamId())
            .withContext(ExamineeNoteContext.EXAM)
            .withNote("Exam note")
            .build();

        when(mockExamineeNoteService.findNoteInExamContext(any(UUID.class)))
            .thenReturn(Optional.of(examineeNote));

        http.perform(get("/exam/{id}/note", examInfo.getExamId().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .param("sessionid", examInfo.getSessionId().toString())
            .param("browserid", examInfo.getBrowserId().toString()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("id", is(42)))
            .andExpect(jsonPath("examId", is(examInfo.getExamId().toString())))
            .andExpect(jsonPath("itemPosition", is(0)))
            .andExpect(jsonPath("note", is("Exam note")));
        verify(mockExamineeNoteService).findNoteInExamContext(any(UUID.class));
    }

    @Test
    public void shouldReturnNoContentSuccessForExamWithNoExamineeNote() throws Exception {
        ExamInfo examInfo = new ExamInfo(UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID());

        when(mockExamineeNoteService.findNoteInExamContext(any(UUID.class)))
            .thenReturn(Optional.empty());

        http.perform(get("/exam/{id}/note", examInfo.getExamId().toString())
            .contentType(MediaType.APPLICATION_JSON)
            .param("sessionid", examInfo.getSessionId().toString())
            .param("browserid", examInfo.getBrowserId().toString()))
            .andExpect(status().isNoContent());
        verify(mockExamineeNoteService).findNoteInExamContext(any(UUID.class));
    }

    @Test
    public void shouldInsertAnExamineeNote() throws Exception {
        ExamInfo examInfo = new ExamInfo(UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID());
        ExamineeNote examineeNote = new ExamineeNote.Builder()
            .withId(42L)
            .withExamId(examInfo.getExamId())
            .withContext(ExamineeNoteContext.ITEM)
            .withItemPosition(5)
            .withNote("Exam note")
            .build();
        String examineeNoteJson = new ObjectMapper().writeValueAsString(examineeNote);

        doNothing().when(mockExamineeNoteService).insert(examineeNote);

        http.perform(put("/exam/{id}/note", examInfo.getExamId())
            .contentType(MediaType.APPLICATION_JSON)
            .content(examineeNoteJson)
            .param("sessionid", examInfo.getSessionId().toString())
            .param("browserid", examInfo.getBrowserId().toString()))
            .andExpect(status().isNoContent());
        verify(mockExamineeNoteService).insert(any(ExamineeNote.class));
    }
}
