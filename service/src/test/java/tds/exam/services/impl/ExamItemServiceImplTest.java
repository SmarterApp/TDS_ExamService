package tds.exam.services.impl;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.common.web.exceptions.NotFoundException;
import tds.exam.ApprovalRequest;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamItemResponseBuilder;
import tds.exam.builder.ExamItemResponseScoreBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.error.ValidationErrorCode;
import tds.exam.repositories.ExamItemCommandRepository;
import tds.exam.repositories.ExamItemQueryRepository;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamPageQueryRepository;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.services.ExamApprovalService;
import tds.exam.services.ExamItemResponseScoringService;
import tds.exam.services.ExamItemService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.isA;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamItemServiceImplTest {

    @Mock
    private ExamItemQueryRepository mockExamItemQueryRepository;

    @Mock
    private ExamItemCommandRepository mockExamItemCommandRepository;

    @Mock
    private ExamPageCommandRepository mockExamPageCommandRepository;

    @Mock
    private ExamPageQueryRepository mockExamPageQueryRepository;

    @Mock
    private ExamQueryRepository mockExamQueryRepository;

    @Mock
    private ExamApprovalService mockExamApprovalService;

    @Mock
    private ExamItemResponseScoringService mockExamItemResponseScoringService;

    private ExamItemService examItemService;

    @Before
    public void setUp() {
        examItemService = new ExamItemServiceImpl(mockExamItemQueryRepository,
            mockExamItemCommandRepository,
            mockExamPageCommandRepository,
            mockExamPageQueryRepository,
            mockExamQueryRepository,
            mockExamApprovalService,
            mockExamItemResponseScoringService);
    }

    @Test
    public void shouldReturnLatestExamPositionForExamId() {
        final UUID examId = UUID.randomUUID();
        final int currentExamPosition = 9;
        when(mockExamItemQueryRepository.getCurrentExamItemPosition(examId))
            .thenReturn(currentExamPosition);

        int examPosition = examItemService.getExamPosition(examId);
        verify(mockExamItemQueryRepository).getCurrentExamItemPosition(examId);

        assertThat(examPosition).isEqualTo(currentExamPosition);
    }

    @Test
    public void shouldInsertAResponse() {
        ApprovalRequest approvalRequest = new ApprovalRequest(UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID());
        int currentPagePosition = 1;
        ExamItemResponse response = new ExamItemResponseBuilder().build();

        Response<ExamApproval> mockExamApprovalResponse = new Response<>(new ExamApproval(approvalRequest.getExamId(),
            new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.IN_PROGRESS),
            null));
        Exam mockExam = new ExamBuilder()
            .withId(approvalRequest.getExamId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.IN_PROGRESS), Instant.now())
            .build();
        ExamPage mockCurrentExamPage = new ExamPageBuilder()
            .withExamId(approvalRequest.getExamId())
            .withPagePosition(currentPagePosition)
            .build();

        when(mockExamApprovalService.getApproval(approvalRequest))
            .thenReturn(mockExamApprovalResponse);
        when(mockExamQueryRepository.getExamById(approvalRequest.getExamId()))
            .thenReturn(Optional.of(mockExam));
        when(mockExamItemResponseScoringService.getScore(any(ExamItemResponse.class)))
            .thenReturn(new ExamItemResponseScoreBuilder().build());
        when(mockExamPageQueryRepository.find(approvalRequest.getExamId(), currentPagePosition))
            .thenReturn(Optional.of(mockCurrentExamPage));

        Response<ExamPage> nextPageResponse = examItemService.insertResponses(approvalRequest, currentPagePosition, response);
        verify(mockExamApprovalService).getApproval(isA(ApprovalRequest.class));
        verify(mockExamQueryRepository).getExamById(approvalRequest.getExamId());
        verify(mockExamItemResponseScoringService).getScore(any(ExamItemResponse.class));
        verify(mockExamPageQueryRepository).find(approvalRequest.getExamId(), currentPagePosition);

        assertThat(nextPageResponse.getData().isPresent()).isTrue();
        assertThat(nextPageResponse.getError().isPresent()).isFalse();

        ExamPage nextPage = nextPageResponse.getData().get();
        assertThat(nextPage.getExamId()).isEqualTo(mockCurrentExamPage.getExamId());
        assertThat(nextPage.getPagePosition()).isEqualTo(mockCurrentExamPage.getPagePosition() + 1);
    }

    @Test
    public void shouldNotInsertAResponseAndReturnValidationErrorBecauseApprovalIsDenied() {
        ApprovalRequest approvalRequest = new ApprovalRequest(UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID());
        int currentPagePosition = 1;
        ExamItemResponse response = new ExamItemResponseBuilder().build();

        Response<ExamApproval> mockApprovalFailure =
            new Response<>(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_SESSION_CLOSED,
                "The session is not available for testing, please check with your test administrator."));
        when(mockExamApprovalService.getApproval(isA(ApprovalRequest.class)))
            .thenReturn(mockApprovalFailure);

        Response<ExamPage> nextPageResponse = examItemService.insertResponses(approvalRequest, currentPagePosition, response);
        verify(mockExamApprovalService).getApproval(any(ApprovalRequest.class));

        assertThat(nextPageResponse.getData().isPresent()).isFalse();
        assertThat(nextPageResponse.getError().isPresent()).isTrue();

        ValidationError error = nextPageResponse.getError().get();
        assertThat(error.getCode()).isEqualTo(mockApprovalFailure.getError().get().getCode());
        assertThat(error.getMessage()).isEqualTo(mockApprovalFailure.getError().get().getMessage());
    }

    @Test
    public void shouldNotInsertAResponseAndReturnValidationErrorBecasueExamIsNotInStartedOrReviewStatus() {
        ApprovalRequest approvalRequest = new ApprovalRequest(UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID());
        int currentPagePosition = 1;
        ExamItemResponse response = new ExamItemResponseBuilder().build();

        Response<ExamApproval> mockExamApprovalResponse = new Response<>(new ExamApproval(approvalRequest.getExamId(),
            new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.IN_PROGRESS),
            null));
        Exam mockExam = new ExamBuilder()
            .withId(approvalRequest.getExamId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PENDING, ExamStatusStage.IN_PROGRESS), Instant.now())
            .build();

        when(mockExamApprovalService.getApproval(approvalRequest))
            .thenReturn(mockExamApprovalResponse);
        when(mockExamQueryRepository.getExamById(approvalRequest.getExamId()))
            .thenReturn(Optional.of(mockExam));

        Response<ExamPage> nextPageResponse = examItemService.insertResponses(approvalRequest, currentPagePosition, response);
        verify(mockExamApprovalService).getApproval(any(ApprovalRequest.class));
        verify(mockExamQueryRepository).getExamById(approvalRequest.getExamId());

        assertThat(nextPageResponse.getData().isPresent()).isFalse();
        assertThat(nextPageResponse.getError().isPresent()).isTrue();

        ValidationError error = nextPageResponse.getError().get();
        assertThat(error.getCode()).isEqualTo(ValidationErrorCode.EXAM_INTERRUPTED);
        assertThat(error.getMessage()).isEqualTo("Your test opportunity has been interrupted. Please check with your Test Administrator to resume your test.");
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenExamDoesNotExist() {
        ApprovalRequest approvalRequest = new ApprovalRequest(UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID());
        int currentPagePosition = 1;
        ExamItemResponse response = new ExamItemResponseBuilder().build();

        Response<ExamApproval> mockExamApprovalResponse = new Response<>(new ExamApproval(approvalRequest.getExamId(),
            new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.IN_PROGRESS),
            null));

        when(mockExamApprovalService.getApproval(approvalRequest))
            .thenReturn(mockExamApprovalResponse);
        when(mockExamQueryRepository.getExamById(approvalRequest.getExamId()))
            .thenReturn(Optional.empty());

        examItemService.insertResponses(approvalRequest, currentPagePosition, response);
        verify(mockExamApprovalService).getApproval(any(ApprovalRequest.class));
        verify(mockExamQueryRepository).getExamById(approvalRequest.getExamId());
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenExamPageForCurrentPositionCannotBeFound() {
        ApprovalRequest approvalRequest = new ApprovalRequest(UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID());
        int currentPagePosition = 1;
        ExamItemResponse response = new ExamItemResponseBuilder().build();

        Response<ExamApproval> mockExamApprovalResponse = new Response<>(new ExamApproval(approvalRequest.getExamId(),
            new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.IN_PROGRESS),
            null));
        Exam mockExam = new ExamBuilder()
            .withId(approvalRequest.getExamId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.IN_PROGRESS), Instant.now())
            .build();

        when(mockExamApprovalService.getApproval(approvalRequest))
            .thenReturn(mockExamApprovalResponse);
        when(mockExamQueryRepository.getExamById(approvalRequest.getExamId()))
            .thenReturn(Optional.of(mockExam));
        when(mockExamItemResponseScoringService.getScore(any(ExamItemResponse.class)))
            .thenReturn(new ExamItemResponseScoreBuilder().build());
        when(mockExamPageQueryRepository.find(approvalRequest.getExamId(), currentPagePosition))
            .thenReturn(Optional.empty());

        examItemService.insertResponses(approvalRequest, currentPagePosition, response);
        verify(mockExamApprovalService).getApproval(isA(ApprovalRequest.class));
        verify(mockExamQueryRepository).getExamById(approvalRequest.getExamId());
        verify(mockExamItemResponseScoringService).getScore(any(ExamItemResponse.class));
        verify(mockExamPageQueryRepository).find(approvalRequest.getExamId(), currentPagePosition);
    }
}
