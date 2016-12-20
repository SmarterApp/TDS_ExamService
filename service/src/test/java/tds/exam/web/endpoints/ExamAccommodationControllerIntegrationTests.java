package tds.exam.web.endpoints;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import tds.common.web.advice.ExceptionAdvice;
import tds.exam.ExamAccommodation;
import tds.exam.builder.ExamAccommodationBuilder;
import tds.exam.services.ExamAccommodationService;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ExamAccommodationController.class)
@Import({ExceptionAdvice.class})
public class ExamAccommodationControllerIntegrationTests {
    @Autowired
    private MockMvc http;

    @MockBean
    private ExamAccommodationService mockExamAccommodationService;

    @Test
    public void shouldReturnAllExamSegmentAccommodationsWhenTypeIsNotProvided() throws Exception {
        UUID mockExamId = UUID.randomUUID();

        ExamAccommodation examAccommodation = new ExamAccommodationBuilder()
            .withExamId(mockExamId)
            .withSegmentKey("unit-test-segment")
            .build();

        when(mockExamAccommodationService.findAccommodations(mockExamId, "unit-test-segment", null)).thenReturn(Collections.singletonList(examAccommodation));

        http.perform(get(new URI(String.format("/exam/%s/unit-test-segment/accommodations", mockExamId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("[0].examId", is(examAccommodation.getExamId().toString())))
            .andExpect(jsonPath("[0].segmentKey", is(examAccommodation.getSegmentKey())))
            .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnExamSegmentAccommodationsForType() throws Exception {
        UUID mockExamId = UUID.randomUUID();

        ExamAccommodation examAccommodation = new ExamAccommodationBuilder()
            .withExamId(mockExamId)
            .withSegmentKey("unit-test-segment")
            .withType("type1")
            .build();

        ExamAccommodation examAccommodation2 = new ExamAccommodationBuilder()
            .withExamId(mockExamId)
            .withSegmentKey("unit-test-segment")
            .withType("type2")
            .build();

        when(mockExamAccommodationService.findAccommodations(mockExamId, "unit-test-segment", "type1", "type2"))
            .thenReturn(Arrays.asList(examAccommodation, examAccommodation2));

        http.perform(get(new URI(String.format("/exam/%s/unit-test-segment/accommodations?type=type1&type=type2", mockExamId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$", hasSize(2)))
            .andExpect(jsonPath("[0].examId", is(examAccommodation.getExamId().toString())))
            .andExpect(jsonPath("[0].segmentKey", is(examAccommodation.getSegmentKey())))
            .andExpect(jsonPath("[0].type", is(examAccommodation.getType())))
            .andExpect(jsonPath("[1].examId", is(examAccommodation2.getExamId().toString())))
            .andExpect(jsonPath("[1].segmentKey", is(examAccommodation2.getSegmentKey())))
            .andExpect(jsonPath("[1].type", is(examAccommodation2.getType())))
            .andExpect(status().isOk());
    }

    @Test
    public void shouldReturnAllExamAccommodations() throws Exception {
        UUID examId = UUID.randomUUID();
        ExamAccommodation examAccommodation = new ExamAccommodationBuilder()
            .withExamId(examId)
            .build();

        when(mockExamAccommodationService.findAllAccommodations(examId)).thenReturn(Collections.singletonList(examAccommodation));

        http.perform(get(new URI(String.format("/exam/%s/accommodations", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("[0].examId", is(examAccommodation.getExamId().toString())))
            .andExpect(jsonPath("[0].type", is(examAccommodation.getType())));

        verify(mockExamAccommodationService).findAllAccommodations(examId);
    }

    @Test
    public void shouldReturnApprovedExamAccommodations() throws Exception {
        UUID examId = UUID.randomUUID();
        ExamAccommodation examAccommodation = new ExamAccommodationBuilder()
            .withExamId(examId)
            .build();

        when(mockExamAccommodationService.findApprovedAccommodations(examId)).thenReturn(Collections.singletonList(examAccommodation));

        http.perform(get(new URI(String.format("/exam/%s/accommodations/approved", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("[0].examId", is(examAccommodation.getExamId().toString())))
            .andExpect(jsonPath("[0].type", is(examAccommodation.getType())));

        verify(mockExamAccommodationService).findApprovedAccommodations(examId);
    }
}
