package tds.exam.web.endpoints;


import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.net.URI;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamItem;
import tds.exam.ExamSegment;
import tds.exam.WebMvcControllerIntegrationTest;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.builder.ExamSegmentBuilder;
import tds.exam.services.ExamSegmentWrapperService;
import tds.exam.wrapper.ExamPageWrapper;
import tds.exam.wrapper.ExamSegmentWrapper;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcControllerIntegrationTest(controllers = ExamSegmentWrapperController.class)
public class ExamSegmentWrapperControllerIntegrationTests {
    @Autowired
    private MockMvc http;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ExamSegmentWrapperService mockExamSegmentWrapperService;

    @Test
    public void shouldFindExamSegmentWrappersForExam() throws Exception {
        final UUID examId = UUID.randomUUID();

        ExamItem examItem = new ExamItemBuilder().build();

        ExamSegment examSegment = new ExamSegmentBuilder()
            .withExamId(examId)
            .withSegmentKey("segmentKey")
            .withSegmentPosition(1)
            .build();

        ExamPageWrapper examPageWrapper = new ExamPageWrapper(new ExamPageBuilder()
            .withExamId(examId)
            .withSegmentKey("segmentKey")
            .withPagePosition(1)
            .build(),
            singletonList(examItem), true);

        when(mockExamSegmentWrapperService.findAllExamSegments(examId)).thenReturn(singletonList(new ExamSegmentWrapper(examSegment, singletonList(examPageWrapper))));

        MvcResult result = http.perform(get(new URI(String.format("/exam/%s/segmentWrappers", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("[0].examSegment.segmentKey", is("segmentKey")))
            .andExpect(jsonPath("[0].examSegment.segmentPosition", is(1)))
            .andExpect(jsonPath("[0].examPages").isArray())
            .andExpect(jsonPath("[0].examPages[0].examPage.segmentKey", is("segmentKey")))
            .andExpect(jsonPath("[0].examPages[0].examItems[0].id", is(examItem.getId().toString())))
            .andReturn();
        String json = result.getResponse().getContentAsString();

        JavaType type = objectMapper.getTypeFactory().constructCollectionType(List.class, ExamSegmentWrapper.class);
        List<ExamSegmentWrapper> wrappers = objectMapper.readValue(json, type);

        assertThat(wrappers).hasSize(1);

        ExamSegmentWrapper wrapper = wrappers.get(0);

        assertThat(wrapper.getExamPages()).hasSize(1);
        assertThat(wrapper.getExamSegment()).isNotNull();
        assertThat(wrapper.getExamPages().get(0).getExamItems()).hasSize(1);
    }

    @Test
    public void shouldFindSingleExamSegmentWrapperForExamAndSegmentPosition() throws Exception {
        final UUID examId = UUID.randomUUID();

        ExamItem examItem = new ExamItemBuilder().build();

        ExamSegment examSegment = new ExamSegmentBuilder()
            .withExamId(examId)
            .withSegmentKey("segmentKey")
            .withSegmentPosition(1)
            .build();

        ExamPageWrapper examPageWrapper = new ExamPageWrapper(new ExamPageBuilder()
            .withExamId(examId)
            .withSegmentKey("segmentKey")
            .withPagePosition(1)
            .build(),
            singletonList(examItem), true);

        ExamPageWrapper examPageWrapper2 = new ExamPageWrapper(new ExamPageBuilder()
            .withExamId(examId)
            .withSegmentKey("segmentKey")
            .withPagePosition(1)
            .build(),
            singletonList(examItem), true);

        when(mockExamSegmentWrapperService.findExamSegment(examId, 1)).thenReturn(Optional.of(new ExamSegmentWrapper(examSegment, Arrays.asList(examPageWrapper, examPageWrapper2))));

        MvcResult result = http.perform(get(new URI(String.format("/exam/%s/segmentWrappers/1", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.examSegment.segmentKey", is("segmentKey")))
            .andExpect(jsonPath("$.examSegment.segmentPosition", is(1)))
            .andExpect(jsonPath("$.examPages").isArray())
            .andExpect(jsonPath("$.examPages[0].examPage.segmentKey", is("segmentKey")))
            .andExpect(jsonPath("$.examPages[0].examItems[0].id", is(examItem.getId().toString())))
            .andReturn();

        String json = result.getResponse().getContentAsString();
        ExamSegmentWrapper wrapper = objectMapper.readValue(json, ExamSegmentWrapper.class);

        assertThat(wrapper.getExamPages()).hasSize(2);
        assertThat(wrapper.getExamSegment()).isNotNull();
        assertThat(wrapper.getExamPages().get(0).getExamItems()).hasSize(1);
    }

    @Test
    public void shouldFindSingleExamSegmentWrapperForExamAndSegmentAndPagePosition() throws Exception {
        final UUID examId = UUID.randomUUID();

        ExamItem examItem = new ExamItemBuilder().build();

        ExamSegment examSegment = new ExamSegmentBuilder()
            .withExamId(examId)
            .withSegmentKey("segmentKey")
            .withSegmentPosition(1)
            .build();

        ExamPageWrapper examPageWrapper = new ExamPageWrapper(new ExamPageBuilder()
            .withExamId(examId)
            .withSegmentKey("segmentKey")
            .withPagePosition(1)
            .build(),
            singletonList(examItem), true);

        when(mockExamSegmentWrapperService.findExamSegmentWithPageAtPosition(examId, 1, 2)).thenReturn(Optional.of(new ExamSegmentWrapper(examSegment, singletonList(examPageWrapper))));

        MvcResult result = http.perform(get(new URI(String.format("/exam/%s/segmentWrappers/1?pagePosition=2", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.examSegment.segmentKey", is("segmentKey")))
            .andExpect(jsonPath("$.examSegment.segmentPosition", is(1)))
            .andExpect(jsonPath("$.examPages").isArray())
            .andExpect(jsonPath("$.examPages[0].examPage.segmentKey", is("segmentKey")))
            .andExpect(jsonPath("$.examPages[0].examItems[0].id", is(examItem.getId().toString())))
            .andReturn();

        String json = result.getResponse().getContentAsString();
        ExamSegmentWrapper wrapper = objectMapper.readValue(json, ExamSegmentWrapper.class);

        assertThat(wrapper.getExamPages()).hasSize(1);
        assertThat(wrapper.getExamSegment()).isNotNull();
        assertThat(wrapper.getExamPages().get(0).getExamItems()).hasSize(1);
    }

    @Test
    public void shouldReturnNotFoundWhenCannotFindExamSegmentWrappersForExam() throws Exception {
        UUID examId = UUID.randomUUID();
        when(mockExamSegmentWrapperService.findAllExamSegments(examId)).thenReturn(Collections.emptyList());

        http.perform(get(new URI(String.format("/exam/%s/segmentWrappers", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnNotFoundWhenCannotFindSingleExamSegmentWrapperForExamAndSegmentPosition() throws Exception {
        UUID examId = UUID.randomUUID();
        when(mockExamSegmentWrapperService.findExamSegment(examId, 1)).thenReturn(Optional.empty());

        http.perform(get(new URI(String.format("/exam/%s/segmentWrappers/1", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }

    @Test
    public void shouldReturnNotFoundWhenCannotFindSingleExamSegmentWrapperForExamAndSegmentAndPagePosition() throws Exception {
        UUID examId = UUID.randomUUID();
        when(mockExamSegmentWrapperService.findExamSegmentWithPageAtPosition(examId, 1, 2)).thenReturn(Optional.empty());

        http.perform(get(new URI(String.format("/exam/%s/segmentWrappers/1?pagePosition=2", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
    }
}
