package tds.exam.web.endpoints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import tds.common.Response;
import tds.exam.ApprovalRequest;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamItemResponseBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.services.ExamItemService;

import static org.mockito.Matchers.any;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamItemControllerTest {
    @Mock
    private ExamItemService mockExamItemService;

    private ExamItemController examItemController;

    @Before
    public void setUp() {
        examItemController = new ExamItemController(mockExamItemService);
    }

    @Test
    public void shouldInsertExamItemResponses() {
        ApprovalRequest mockApprovalRequest = new ApprovalRequest(UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID(),
            "UNIT_TEST");
        ExamItem mockExamItem = new ExamItemBuilder().build();
        List<ExamItem> mockExamItems = Arrays.asList(mockExamItem);
        ExamPage mockNextExamPage = new ExamPageBuilder()
            .withExamId(mockApprovalRequest.getExamId())
            .withPagePosition(2)
            .withExamItems(mockExamItems)
            .build();

        ArgumentCaptor<ApprovalRequest> approvalRequestArgumentCaptor = ArgumentCaptor.forClass(ApprovalRequest.class);
        when(mockExamItemService.insertResponses(isA(ApprovalRequest.class),
            isA(Integer.class),
            any(ExamItemResponse[].class)))
            .thenReturn(new Response<>(mockNextExamPage));

        ExamItemResponse[] responses = new ExamItemResponse[] { new ExamItemResponseBuilder().build() };

        ResponseEntity<Response<ExamPage>> result = examItemController.insertResponses(mockApprovalRequest.getExamId(),
            1,
            mockApprovalRequest.getSessionId(),
            mockApprovalRequest.getBrowserId(),
            mockApprovalRequest.getClientName(),
            responses);
        verify(mockExamItemService).insertResponses(approvalRequestArgumentCaptor.capture(), isA(Integer.class), any(ExamItemResponse[].class));

        assertThat(approvalRequestArgumentCaptor.getValue()).isEqualTo(mockApprovalRequest);
        assertThat(result.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(result.getBody().getData().isPresent()).isTrue();
        assertThat(result.getBody().getError().isPresent()).isFalse();
    }
}
