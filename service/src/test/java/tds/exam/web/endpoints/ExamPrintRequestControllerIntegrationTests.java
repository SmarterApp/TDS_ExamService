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


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;
import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamPrintRequest;
import tds.exam.ExamPrintRequestStatus;
import tds.exam.ExpandableExamPrintRequest;
import tds.exam.WebMvcControllerIntegrationTest;
import tds.exam.builder.ExamBuilder;
import tds.exam.services.ExamPrintRequestService;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.hamcrest.CoreMatchers.is;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcControllerIntegrationTest(controllers = ExamPrintRequestController.class)
public class ExamPrintRequestControllerIntegrationTests {
    @Autowired
    private MockMvc http;

    @MockBean
    private ExamPrintRequestService mockExamPrintRequestService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void shouldCreateExamPrintRequest() throws Exception {
        ExamPrintRequest printRequest = random(ExamPrintRequest.class);
        ObjectWriter ow = objectMapper
            .writer().withDefaultPrettyPrinter();

        http.perform(post(new URI("/exam/print"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(ow.writeValueAsString(printRequest)))
            .andExpect(status().isNoContent());

        verify(mockExamPrintRequestService).insert(any());
    }

    @Test
    public void shouldDenyPrintRequest() throws Exception {
        final UUID id = UUID.randomUUID();
        final String reason = "I don't like your tie";
        ExamPrintRequest examPrintRequest = random(ExamPrintRequest.class);
        when(mockExamPrintRequestService.updateAndGetRequest(ExamPrintRequestStatus.DENIED, id, reason)).thenReturn(Optional.of(examPrintRequest));

        http.perform(put("/exam/print/deny/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .content(reason))
            .andExpect(status().isOk())
            .andExpect(jsonPath("id", is(examPrintRequest.getId().toString())))
            .andExpect(jsonPath("sessionId", is(examPrintRequest.getSessionId().toString())))
            .andExpect(jsonPath("type", is(examPrintRequest.getType())))
            .andExpect(jsonPath("value", is(examPrintRequest.getValue())))
            .andExpect(jsonPath("reasonDenied", is(examPrintRequest.getReasonDenied())))
            .andExpect(jsonPath("description", is(examPrintRequest.getDescription())))
            .andExpect(jsonPath("itemPosition", is(examPrintRequest.getItemPosition())))
            .andExpect(jsonPath("pagePosition", is(examPrintRequest.getPagePosition())))
            .andExpect(jsonPath("examId", is(examPrintRequest.getExamId().toString())));

        verify(mockExamPrintRequestService).updateAndGetRequest(ExamPrintRequestStatus.DENIED, id, reason);
    }

    @Test
    public void shouldReturnNotFoundErrorForNoRequestFoundDenied() throws Exception {
        final UUID id = UUID.randomUUID();
        final String reason = "A reason";
        when(mockExamPrintRequestService.updateAndGetRequest(ExamPrintRequestStatus.DENIED, id, reason)).thenReturn(Optional.empty());
        http.perform(put("/exam/print/deny/{id}", id)
            .content(reason)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
        verify(mockExamPrintRequestService).updateAndGetRequest(ExamPrintRequestStatus.DENIED, id, reason);
    }

    @Test
    public void shouldRetrieveListOfExamPrintRequests() throws Exception {
        final ExamPrintRequest examPrintRequest = random(ExamPrintRequest.class);
        final UUID examId = UUID.randomUUID();
        final UUID sessionId = UUID.randomUUID();

        when(mockExamPrintRequestService.findUnfulfilledRequests(examId, sessionId)).thenReturn(Arrays.asList(examPrintRequest));

        http.perform(get("/exam/print/{sessionId}/{examId}", sessionId, examId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("[0].id", is(examPrintRequest.getId().toString())))
            .andExpect(jsonPath("[0].sessionId", is(examPrintRequest.getSessionId().toString())))
            .andExpect(jsonPath("[0].type", is(examPrintRequest.getType())))
            .andExpect(jsonPath("[0].value", is(examPrintRequest.getValue())))
            .andExpect(jsonPath("[0].reasonDenied", is(examPrintRequest.getReasonDenied())))
            .andExpect(jsonPath("[0].description", is(examPrintRequest.getDescription())))
            .andExpect(jsonPath("[0].itemPosition", is(examPrintRequest.getItemPosition())))
            .andExpect(jsonPath("[0].pagePosition", is(examPrintRequest.getPagePosition())))
            .andExpect(jsonPath("[0].examId", is(examPrintRequest.getExamId().toString())));

        verify(mockExamPrintRequestService).findUnfulfilledRequests(examId, sessionId);
    }

    @Test
    public void shouldRetrieveListOfApprovedExamPrintRequests() throws Exception {
        final ExamPrintRequest examPrintRequest = random(ExamPrintRequest.class);
        final UUID sessionId = UUID.randomUUID();

        when(mockExamPrintRequestService.findApprovedRequests(sessionId)).thenReturn(Arrays.asList(examPrintRequest));

        http.perform(get("/exam/print/approved/{sessionId}", sessionId)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("[0].id", is(examPrintRequest.getId().toString())))
            .andExpect(jsonPath("[0].sessionId", is(examPrintRequest.getSessionId().toString())))
            .andExpect(jsonPath("[0].type", is(examPrintRequest.getType())))
            .andExpect(jsonPath("[0].value", is(examPrintRequest.getValue())))
            .andExpect(jsonPath("[0].reasonDenied", is(examPrintRequest.getReasonDenied())))
            .andExpect(jsonPath("[0].description", is(examPrintRequest.getDescription())))
            .andExpect(jsonPath("[0].itemPosition", is(examPrintRequest.getItemPosition())))
            .andExpect(jsonPath("[0].pagePosition", is(examPrintRequest.getPagePosition())))
            .andExpect(jsonPath("[0].examId", is(examPrintRequest.getExamId().toString())));

        verify(mockExamPrintRequestService).findApprovedRequests(sessionId);
    }

    @Test
    public void shouldApproveAndFindExpandablePrintRequest() throws Exception {
        final UUID id = UUID.randomUUID();
        final ExamPrintRequest examPrintRequest = new ExamPrintRequest.Builder(id)
            .fromExamPrintRequest(random(ExamPrintRequest.class))
            .build();
        final Exam exam = new ExamBuilder().build();
        final ExpandableExamPrintRequest expandableExamPrintRequest = new ExpandableExamPrintRequest.Builder(examPrintRequest)
            .withExam(exam)
            .build();

        when(mockExamPrintRequestService.updateAndGetRequest(ExamPrintRequestStatus.APPROVED, id, null,
            ExpandableExamPrintRequest.EXPANDABLE_PARAMS_PRINT_REQUEST_WITH_EXAM)).thenReturn(Optional.of(expandableExamPrintRequest));

        http.perform(put("/exam/print/approve/{id}", id)
            .contentType(MediaType.APPLICATION_JSON)
            .param("expandableProperties", ExpandableExamPrintRequest.EXPANDABLE_PARAMS_PRINT_REQUEST_WITH_EXAM))
            .andExpect(status().isOk())
            .andExpect(jsonPath("examPrintRequest.id", is(examPrintRequest.getId().toString())))
            .andExpect(jsonPath("examPrintRequest.sessionId", is(examPrintRequest.getSessionId().toString())))
            .andExpect(jsonPath("examPrintRequest.type", is(examPrintRequest.getType())))
            .andExpect(jsonPath("examPrintRequest.value", is(examPrintRequest.getValue())))
            .andExpect(jsonPath("examPrintRequest.reasonDenied", is(examPrintRequest.getReasonDenied())))
            .andExpect(jsonPath("examPrintRequest.description", is(examPrintRequest.getDescription())))
            .andExpect(jsonPath("examPrintRequest.itemPosition", is(examPrintRequest.getItemPosition())))
            .andExpect(jsonPath("examPrintRequest.pagePosition", is(examPrintRequest.getPagePosition())))
            .andExpect(jsonPath("examPrintRequest.examId", is(examPrintRequest.getExamId().toString())))
            .andExpect(jsonPath("exam.id", is(exam.getId().toString())));

        verify(mockExamPrintRequestService).updateAndGetRequest(ExamPrintRequestStatus.APPROVED, id, null,
            ExpandableExamPrintRequest.EXPANDABLE_PARAMS_PRINT_REQUEST_WITH_EXAM);
    }

    @Test
    public void shouldReturnNotFoundErrorForNoRequestFound() throws Exception {
        final UUID id = UUID.randomUUID();
        when(mockExamPrintRequestService.updateAndGetRequest(ExamPrintRequestStatus.APPROVED, id, null, null)).thenReturn(Optional.empty());
        http.perform(put("/exam/print/approve/{id}", id)
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isNotFound());
        verify(mockExamPrintRequestService).updateAndGetRequest(ExamPrintRequestStatus.APPROVED, id, null, null);
    }
}
