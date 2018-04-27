/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

package tds.exam.web.endpoints;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.collect.ImmutableMap;
import org.junit.Before;
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.ApproveAccommodationsRequest;
import tds.exam.Exam;
import tds.exam.ExamAssessmentMetadata;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusRequest;
import tds.exam.ExamStatusStage;
import tds.exam.SegmentApprovalRequest;
import tds.exam.WebMvcControllerIntegrationTest;
import tds.exam.builder.ExamBuilder;
import tds.exam.error.ValidationErrorCode;
import tds.exam.services.ExamApprovalService;
import tds.exam.services.ExamPageService;
import tds.exam.services.ExamService;
import tds.exam.web.interceptors.VerifyAccessInterceptor;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static io.github.benas.randombeans.api.EnhancedRandom.randomSetOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcControllerIntegrationTest(controllers = ExamController.class)
public class ExamControllerIntegrationTests {

    @Autowired
    private MockMvc http;

    @MockBean
    private ExamService mockExamService;

    @MockBean
    private ExamPageService mockExamPageService;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VerifyAccessInterceptor mockVerifyAccessInterceptor;

    @MockBean
    private ExamApprovalService mockExamApprovalService;

    private ObjectWriter ow;

    @Before
    public void setUp() {
        ow = objectMapper.writer().withDefaultPrettyPrinter();
    }

    @Test
    public void shouldReturnExam() throws Exception {
        UUID examId = UUID.randomUUID();
        Exam exam = new ExamBuilder().withId(examId).build();

        when(mockExamService.findExam(examId)).thenReturn(Optional.of(exam));

        http.perform(get(new URI(String.format("/exam/%s", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("id", is(examId.toString())));

        verify(mockExamService).findExam(examId);
    }

    @Test
    public void shouldReturnNotFoundIfExamCannotBeFound() throws Exception {
        UUID examId = UUID.randomUUID();

        when(mockExamService.findExam(examId)).thenReturn(Optional.empty());

        http.perform(get(new URI(String.format("/exam/%s", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());

        verify(mockExamService).findExam(examId);
    }

    @Test
    public void shouldPauseAnExam() throws Exception {
        UUID examId = UUID.randomUUID();

        when(mockExamService.updateExamStatus(examId,
            new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE))).thenReturn(Optional.empty());

        http.perform(put(new URI(String.format("/exam/%s/pause", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent())
            .andExpect(header().string("Location", String.format("http://localhost/exam/%s", examId)));

        verify(mockExamService).updateExamStatus(examId,
            new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE));
    }

    @Test
    public void shouldReturnAnErrorWhenAttemptingToPauseAnExamInAnInvalidTransitionState() throws Exception {
        UUID examId = UUID.randomUUID();

        when(mockExamService.updateExamStatus(examId, new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE)))
            .thenReturn(Optional.of(new ValidationError(ValidationErrorCode.EXAM_STATUS_TRANSITION_FAILURE, "Bad transition from foo to bar")));

        http.perform(put(new URI(String.format("/exam/%s/pause", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity())
            .andExpect(jsonPath("errors").isNotEmpty())
            .andExpect(jsonPath("errors").isArray())
            .andExpect(jsonPath("errors[0].code", is("badStatusTransition")))
            .andExpect(jsonPath("errors[0].message", is("Bad transition from foo to bar")));
    }

    @Test
    public void shouldPauseAllExamsInASession() throws Exception {
        UUID sessionId = UUID.randomUUID();
        doNothing().when(mockExamService).pauseAllExamsInSession(sessionId);

        http.perform(put(new URI(String.format("/exam/pause/%s", sessionId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(mockExamService).pauseAllExamsInSession(sessionId);
    }

    @Test
    public void shouldUpdateExamStatus() throws Exception {
        final UUID examId = UUID.randomUUID();
        final ExamStatusRequest request = new ExamStatusRequest(random(ExamStatusCode.class), null);

        when(mockExamService.updateExamStatus(eq(examId), any(), (String) isNull())).thenReturn(Optional.empty());

        http.perform(put(new URI(String.format("/exam/%s/status/", examId)))
            .content(ow.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(mockExamService).updateExamStatus(eq(examId), any(), (String) isNull());
    }

    @Test
    public void shouldFailStatusUpdateWithError() throws Exception {
        final UUID examId = UUID.randomUUID();
        final ExamStatusRequest request = new ExamStatusRequest(random(ExamStatusCode.class), null);

        when(mockExamService.updateExamStatus(eq(examId), any(), (String) isNull()))
            .thenReturn(Optional.of(new ValidationError("Some", "Error")));

        http.perform(put(new URI(String.format("/exam/%s/status/", examId)))
            .content(ow.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isUnprocessableEntity());

        verify(mockExamService).updateExamStatus(eq(examId), any(), (String) isNull());
    }

    @Test
    public void shouldThrowWithNoStatusProvided() throws Exception {
        final UUID examId = UUID.randomUUID();

        http.perform(put(new URI(String.format("/exam/%s/status/", examId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isInternalServerError());

    }

    @Test
    public void shouldWaitForSegmentSuccessfully() throws Exception {
        UUID examId = UUID.randomUUID();
        SegmentApprovalRequest request = random(SegmentApprovalRequest.class);
        when(mockExamService.waitForSegmentApproval(eq(examId), any())).thenReturn(Optional.empty());

        http.perform(put(new URI(String.format("/exam/%s/segmentApproval/", examId)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(ow.writeValueAsString(request)))
            .andExpect(header().string("Location", String.format("http://localhost/exam/%s", examId)))
            .andExpect(status().isNoContent());

        verify(mockExamService).waitForSegmentApproval(eq(examId), any());
    }

    @Test
    public void shouldReturnErrorWaitForSegment() throws Exception {
        UUID examId = UUID.randomUUID();
        SegmentApprovalRequest request = random(SegmentApprovalRequest.class);
        when(mockExamService.waitForSegmentApproval(eq(examId), any())).thenReturn(Optional.of(new ValidationError("some", "error")));

        http.perform(put(new URI(String.format("/exam/%s/segmentApproval/", examId)))
            .contentType(MediaType.APPLICATION_JSON)
            .content(ow.writeValueAsString(request)))
            .andExpect(status().isUnprocessableEntity());


        verify(mockExamService).waitForSegmentApproval(eq(examId), any());
    }

    @Test
    public void shouldApproveAccommodationsAndReturnNoContentWithNoErrors() throws Exception {
        final UUID examId = UUID.randomUUID();
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        Map<Integer, Set<String>> mockMap = ImmutableMap.of(0, randomSetOf(2, String.class));
        ApproveAccommodationsRequest request = new ApproveAccommodationsRequest(sessionId, browserId, mockMap);

        when(mockExamService.updateExamAccommodationsAndExam(examId, request)).thenReturn(Optional.empty());

        http.perform(post(new URI(String.format("/exam/%s/accommodations", examId)))
            .content(ow.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        verify(mockExamService).updateExamAccommodationsAndExam(examId, request);
    }

    @Test
    public void shouldReturnUnprocessableEntityWithError() throws Exception {
        final UUID examId = UUID.randomUUID();
        final UUID sessionId = UUID.randomUUID();
        final UUID browserId = UUID.randomUUID();
        final String errorCode = "ErrorCode";
        final String errorMsg = "Error!";
        Map<Integer, Set<String>> mockMap = ImmutableMap.of(0, randomSetOf(2, String.class));
        ApproveAccommodationsRequest request = new ApproveAccommodationsRequest(sessionId, browserId, mockMap);

        when(mockExamService.updateExamAccommodationsAndExam(examId, request)).thenReturn(Optional.of(new ValidationError(errorCode, errorMsg)));

        http.perform(post(new URI(String.format("/exam/%s/accommodations", examId)))
            .content(ow.writeValueAsString(request))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("errors[0].code", is(errorCode)))
            .andExpect(jsonPath("errors[0].message", is(errorMsg)))
            .andExpect(status().isUnprocessableEntity());

        verify(mockExamService).updateExamAccommodationsAndExam(examId, request);
    }

    @Test
    public void shouldGetEligibleExamAssessments() throws Exception {
        final long studentId = 2112;
        final UUID sessionId = UUID.randomUUID();
        final String grade = "3";

        ExamAssessmentMetadata examAssessmentMetadata1 = random(ExamAssessmentMetadata.class);
        ExamAssessmentMetadata examAssessmentMetadata2 = random(ExamAssessmentMetadata.class);

        when(mockExamService.findExamAssessmentMetadata(studentId, sessionId, grade))
            .thenReturn(new Response<>(Arrays.asList(examAssessmentMetadata1, examAssessmentMetadata2)));

        MvcResult result = http.perform(get(new URI("/exam/metadata"))
            .contentType(MediaType.APPLICATION_JSON)
            .param("studentId", String.valueOf(studentId))
            .param("sessionId", sessionId.toString())
            .param("grade", grade))
            .andExpect(status().isOk())
            .andExpect(jsonPath("data.[0].assessmentId", is(examAssessmentMetadata1.getAssessmentId())))
            .andExpect(jsonPath("data.[1].assessmentId", is(examAssessmentMetadata2.getAssessmentId())))
            .andReturn();

        Response<List<ExamAssessmentMetadata>> parsedResponse =
            objectMapper.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<Response<List<ExamAssessmentMetadata>>>() {});

        verify(mockExamService).findExamAssessmentMetadata(studentId, sessionId, grade);
        assertThat(parsedResponse.getError().isPresent()).isFalse();
        List<ExamAssessmentMetadata> parsedExamAssessmentMetadata = parsedResponse.getData().get();

        assertThat(parsedExamAssessmentMetadata.get(0).getAssessmentId()).isEqualTo(examAssessmentMetadata1.getAssessmentId());
        assertThat(parsedExamAssessmentMetadata.get(1).getAssessmentId()).isEqualTo(examAssessmentMetadata2.getAssessmentId());
    }

    @Test
    public void shouldFailToGetEligibleExamAssessments() throws Exception {
        final long studentId = 2112;
        final UUID sessionId = UUID.randomUUID();
        final String grade = "3";
        ValidationError error = random(ValidationError.class);
        when(mockExamService.findExamAssessmentMetadata(studentId, sessionId, grade))
            .thenReturn(new Response<>(error));

        MvcResult result = http.perform(get(new URI("/exam/metadata"))
            .contentType(MediaType.APPLICATION_JSON)
            .param("studentId", String.valueOf(studentId))
            .param("sessionId", sessionId.toString())
            .param("grade", grade))
            .andExpect(status().isUnprocessableEntity())
            .andReturn();

        Response<List<ExamAssessmentMetadata>> parsedResponse =
            objectMapper.readValue(result.getResponse().getContentAsByteArray(), new TypeReference<Response<List<ExamAssessmentMetadata>>>() {});

        verify(mockExamService).findExamAssessmentMetadata(studentId, sessionId, grade);
        assertThat(parsedResponse.getError().isPresent()).isTrue();
        ValidationError validationError = parsedResponse.getError().get();

        assertThat(validationError.getCode()).isEqualTo(error.getCode());
    }
}
