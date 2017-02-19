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
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.ExamInfo;
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
        ExamInfo examInfo = new ExamInfo(UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID());
        int currentPagePosition = 1;
        ExamItemResponse response = new ExamItemResponseBuilder().build();

        Response<ExamApproval> mockExamApprovalResponse = new Response<>(new ExamApproval(examInfo.getExamId(),
            new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.IN_PROGRESS),
            null));
        Exam mockExam = new ExamBuilder()
            .withId(examInfo.getExamId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.IN_PROGRESS), Instant.now())
            .build();
        ExamPage mockCurrentExamPage = new ExamPageBuilder()
            .withExamId(examInfo.getExamId())
            .withPagePosition(currentPagePosition)
            .build();

        when(mockExamQueryRepository.getExamById(examInfo.getExamId()))
            .thenReturn(Optional.of(mockExam));
        when(mockExamApprovalService.verifyAccess(isA(ExamInfo.class), isA(Exam.class)))
            .thenReturn(Optional.empty());
        when(mockExamPageQueryRepository.find(examInfo.getExamId(), currentPagePosition))
            .thenReturn(Optional.of(mockCurrentExamPage));
        when(mockExamItemResponseScoringService.getScore(any(ExamItemResponse.class)))
            .thenReturn(new ExamItemResponseScoreBuilder().build());

        Response<ExamPage> nextPageResponse = examItemService.insertResponses(examInfo, currentPagePosition, response);
        verify(mockExamQueryRepository).getExamById(examInfo.getExamId());
        verify(mockExamApprovalService).verifyAccess(isA(ExamInfo.class), isA(Exam.class));
        verify(mockExamItemResponseScoringService).getScore(any(ExamItemResponse.class));
        verify(mockExamPageQueryRepository).find(examInfo.getExamId(), currentPagePosition);

        assertThat(nextPageResponse.getData().isPresent()).isTrue();
        assertThat(nextPageResponse.getError().isPresent()).isFalse();

        ExamPage nextPage = nextPageResponse.getData().get();
        assertThat(nextPage.getExamId()).isEqualTo(mockCurrentExamPage.getExamId());
        assertThat(nextPage.getPagePosition()).isEqualTo(mockCurrentExamPage.getPagePosition() + 1);
    }

    @Test
    public void shouldNotInsertAResponseAndReturnValidationErrorBecauseApprovalIsDenied() {
        ExamInfo examInfo = new ExamInfo(UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID());
        int currentPagePosition = 1;
        ExamItemResponse response = new ExamItemResponseBuilder().build();

        Exam mockExam = new ExamBuilder()
            .withId(examInfo.getExamId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.IN_PROGRESS), Instant.now())
            .build();
        ValidationError mockApprovalFailure = new ValidationError(ValidationErrorCode.EXAM_APPROVAL_SESSION_CLOSED,
                "The session is not available for testing, please check with your test administrator.");

        when(mockExamQueryRepository.getExamById(examInfo.getExamId()))
            .thenReturn(Optional.of(mockExam));
        when(mockExamApprovalService.verifyAccess(isA(ExamInfo.class), isA(Exam.class)))
            .thenReturn(Optional.of(mockApprovalFailure));

        Response<ExamPage> nextPageResponse = examItemService.insertResponses(examInfo, currentPagePosition, response);
        verify(mockExamQueryRepository).getExamById(examInfo.getExamId());
        verify(mockExamApprovalService).verifyAccess(isA(ExamInfo.class), isA(Exam.class));

        assertThat(nextPageResponse.getData().isPresent()).isFalse();
        assertThat(nextPageResponse.getError().isPresent()).isTrue();

        ValidationError error = nextPageResponse.getError().get();
        assertThat(error.getCode()).isEqualTo(mockApprovalFailure.getCode());
        assertThat(error.getMessage()).isEqualTo(mockApprovalFailure.getMessage());
    }

    @Test
    public void shouldNotInsertAResponseAndReturnValidationErrorBecasueExamIsNotInStartedOrReviewStatus() {
        ExamInfo examInfo = new ExamInfo(UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID());
        int currentPagePosition = 1;
        ExamItemResponse response = new ExamItemResponseBuilder().build();

        Exam mockExam = new ExamBuilder()
            .withId(examInfo.getExamId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PENDING, ExamStatusStage.IN_PROGRESS), Instant.now())
            .build();

        when(mockExamApprovalService.verifyAccess(isA(ExamInfo.class), isA(Exam.class)))
            .thenReturn(Optional.empty());
        when(mockExamQueryRepository.getExamById(examInfo.getExamId()))
            .thenReturn(Optional.of(mockExam));

        Response<ExamPage> nextPageResponse = examItemService.insertResponses(examInfo, currentPagePosition, response);
        verify(mockExamApprovalService).verifyAccess(isA(ExamInfo.class), isA(Exam.class));
        verify(mockExamQueryRepository).getExamById(examInfo.getExamId());

        assertThat(nextPageResponse.getData().isPresent()).isFalse();
        assertThat(nextPageResponse.getError().isPresent()).isTrue();

        ValidationError error = nextPageResponse.getError().get();
        assertThat(error.getCode()).isEqualTo(ValidationErrorCode.EXAM_INTERRUPTED);
        assertThat(error.getMessage()).isEqualTo("Your test opportunity has been interrupted. Please check with your Test Administrator to resume your test.");
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenExamDoesNotExist() {
        ExamInfo examInfo = new ExamInfo(UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID());
        int currentPagePosition = 1;
        ExamItemResponse response = new ExamItemResponseBuilder().build();

        Response<ExamApproval> mockExamApprovalResponse = new Response<>(new ExamApproval(examInfo.getExamId(),
            new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.IN_PROGRESS),
            null));

        when(mockExamApprovalService.getApproval(examInfo))
            .thenReturn(mockExamApprovalResponse);
        when(mockExamQueryRepository.getExamById(examInfo.getExamId()))
            .thenReturn(Optional.empty());

        examItemService.insertResponses(examInfo, currentPagePosition, response);
        verify(mockExamApprovalService).getApproval(any(ExamInfo.class));
        verify(mockExamQueryRepository).getExamById(examInfo.getExamId());
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenExamPageForCurrentPositionCannotBeFound() {
        ExamInfo examInfo = new ExamInfo(UUID.randomUUID(),
            UUID.randomUUID(),
            UUID.randomUUID());
        int currentPagePosition = 1;
        ExamItemResponse response = new ExamItemResponseBuilder().build();

        Exam mockExam = new ExamBuilder()
            .withId(examInfo.getExamId())
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.IN_PROGRESS), Instant.now())
            .build();

        when(mockExamQueryRepository.getExamById(examInfo.getExamId()))
            .thenReturn(Optional.of(mockExam));
        when(mockExamApprovalService.verifyAccess(isA(ExamInfo.class), isA(Exam.class)))
            .thenReturn(Optional.empty());
        when(mockExamPageQueryRepository.find(examInfo.getExamId(), currentPagePosition))
            .thenReturn(Optional.empty());

        examItemService.insertResponses(examInfo, currentPagePosition, response);
        verify(mockExamQueryRepository).getExamById(examInfo.getExamId());
        verify(mockExamApprovalService).verifyAccess(isA(ExamInfo.class), isA(Exam.class));
        verify(mockExamPageQueryRepository).find(examInfo.getExamId(), currentPagePosition);
    }
}
