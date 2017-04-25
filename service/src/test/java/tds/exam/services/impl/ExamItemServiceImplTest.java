package tds.exam.services.impl;

import com.google.common.collect.ImmutableMap;
import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.common.web.exceptions.NotFoundException;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.ExamInfo;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamItemResponseBuilder;
import tds.exam.builder.ExamItemResponseScoreBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.error.ValidationErrorCode;
import tds.exam.repositories.ExamItemCommandRepository;
import tds.exam.repositories.ExamItemQueryRepository;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamPageQueryRepository;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.services.ExamItemResponseScoringService;
import tds.exam.services.ExamItemService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.never;
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
    private ExamItemResponseScoringService mockExamItemResponseScoringService;

    private ExamItemService examItemService;

    @Captor
    private ArgumentCaptor<ExamItemResponse> examItemResponseCaptor;

    @Before
    public void setUp() {
        examItemService = new ExamItemServiceImpl(mockExamItemQueryRepository,
            mockExamItemCommandRepository,
            mockExamPageCommandRepository,
            mockExamPageQueryRepository,
            mockExamQueryRepository,
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
        when(mockExamPageQueryRepository.find(examInfo.getExamId(), currentPagePosition))
            .thenReturn(Optional.of(mockCurrentExamPage));
        when(mockExamItemResponseScoringService.getScore(any(ExamItemResponse.class)))
            .thenReturn(new ExamItemResponseScoreBuilder().build());

        Response<ExamPage> nextPageResponse = examItemService.insertResponses(examInfo.getExamId(), currentPagePosition, response);
        verify(mockExamQueryRepository).getExamById(examInfo.getExamId());
        verify(mockExamItemResponseScoringService).getScore(any(ExamItemResponse.class));
        verify(mockExamPageQueryRepository).find(examInfo.getExamId(), currentPagePosition);

        assertThat(nextPageResponse.getData().isPresent()).isTrue();
        assertThat(nextPageResponse.getError().isPresent()).isFalse();

        ExamPage nextPage = nextPageResponse.getData().get();
        assertThat(nextPage.getExamId()).isEqualTo(mockCurrentExamPage.getExamId());
        assertThat(nextPage.getPagePosition()).isEqualTo(mockCurrentExamPage.getPagePosition() + 1);
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenExamDoesNotExist() {
        UUID mockExamId = UUID.randomUUID();
        int currentPagePosition = 1;
        ExamItemResponse response = new ExamItemResponseBuilder().build();

        when(mockExamQueryRepository.getExamById(mockExamId))
            .thenReturn(Optional.empty());

        examItemService.insertResponses(mockExamId, currentPagePosition, response);
        verify(mockExamQueryRepository).getExamById(mockExamId);
    }

    @Test
    public void shouldReturnResponseCountMap() {
        UUID examId1 = UUID.randomUUID();
        UUID examId2 = UUID.randomUUID();
        Map<UUID, Integer> mockMap = ImmutableMap.of(
            UUID.randomUUID(), 1,
            UUID.randomUUID(), 2
        );
        when(mockExamItemQueryRepository.getResponseCounts(examId1, examId2)).thenReturn(mockMap);
        Map<UUID, Integer> returnMap = examItemService.getResponseCounts(examId1, examId2);
        assertThat(returnMap).hasSize(2);
        verify(mockExamItemQueryRepository).getResponseCounts(examId1, examId2);
    }

    @Test(expected = NotFoundException.class)
    public void shouldThrowNotFoundExceptionWhenExamPageForCurrentPositionCannotBeFound() {
        UUID mockExamId = UUID.randomUUID();
        int currentPagePosition = 1;
        ExamItemResponse response = new ExamItemResponseBuilder().build();

        Exam mockExam = new ExamBuilder()
            .withId(mockExamId)
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.IN_PROGRESS), Instant.now())
            .build();

        when(mockExamQueryRepository.getExamById(mockExamId))
            .thenReturn(Optional.of(mockExam));
        when(mockExamPageQueryRepository.find(mockExamId, currentPagePosition))
            .thenReturn(Optional.empty());

        examItemService.insertResponses(mockExamId, currentPagePosition, response);
        verify(mockExamQueryRepository).getExamById(mockExamId);
        verify(mockExamPageQueryRepository).find(mockExamId, currentPagePosition);
    }

    @Test
    public void shouldMarkItemForReviewSuccessfully() {
        final UUID examId = UUID.randomUUID();
        final int position = 7;
        final boolean mark = true;

        ExamItem examItem = new ExamItemBuilder().withId(UUID.randomUUID())
            .withResponse(new ExamItemResponse.Builder()
                .withSequence(3)
                .withResponse("the response")
                .withMarkedForReview(false)
                .build()
            )
            .build();

        when(mockExamItemQueryRepository.findExamItemAndResponse(examId, position)).thenReturn(Optional.of(examItem));

        Optional<ValidationError> maybeError = examItemService.markForReview(examId, position, mark);

        verify(mockExamItemQueryRepository).findExamItemAndResponse(examId, position);
        verify(mockExamItemCommandRepository).insertResponses(examItemResponseCaptor.capture());

        assertThat(maybeError).isNotPresent();
        ExamItemResponse updatedResponse = examItemResponseCaptor.getValue();
        assertThat(updatedResponse.isMarkedForReview()).isTrue();
    }

    @Test
    public void shouldFailToMarkItemForReviewNoExamItem() {
        final UUID examId = UUID.randomUUID();
        final int position = 7;
        final boolean mark = true;

        when(mockExamItemQueryRepository.findExamItemAndResponse(examId, position)).thenReturn(Optional.empty());

        Optional<ValidationError> maybeError = examItemService.markForReview(examId, position, mark);

        verify(mockExamItemQueryRepository).findExamItemAndResponse(examId, position);
        verify(mockExamItemCommandRepository, never()).insertResponses(any());

        assertThat(maybeError).isPresent();
        assertThat(maybeError.get().getCode()).isEqualTo(ValidationErrorCode.EXAM_ITEM_DOES_NOT_EXIST);
    }

    @Test
    public void shouldFailToMarkItemForReviewNoExamItemResponse() {
        final UUID examId = UUID.randomUUID();
        final int position = 7;
        final boolean mark = true;
        ExamItem examItem = new ExamItemBuilder().withId(UUID.randomUUID()).build();

        when(mockExamItemQueryRepository.findExamItemAndResponse(examId, position)).thenReturn(Optional.of(examItem));

        Optional<ValidationError> maybeError = examItemService.markForReview(examId, position, mark);

        verify(mockExamItemQueryRepository).findExamItemAndResponse(examId, position);
        verify(mockExamItemCommandRepository, never()).insertResponses(any());

        assertThat(maybeError).isPresent();
        assertThat(maybeError.get().getCode()).isEqualTo(ValidationErrorCode.EXAM_ITEM_RESPONSE_DOES_NOT_EXIST);
    }

    @Test
    public void shouldFindItemAndResponsesForExam() {
        final UUID examId = UUID.randomUUID();
        ExamItem examItem = new ExamItemBuilder().withId(UUID.randomUUID()).build();

        when(mockExamItemQueryRepository.findExamItemAndResponses(examId)).thenReturn(Collections.singletonList(examItem));

        assertThat(examItemService.findExamItemAndResponses(examId)).containsExactly(examItem);
    }

    @Test
    public void shouldFindItemAndResponsesForExamAndPosition() {
        final UUID examId = UUID.randomUUID();
        final int position = 7;
        ExamItem examItem = new ExamItemBuilder().withId(UUID.randomUUID()).build();

        when(mockExamItemQueryRepository.findExamItemAndResponse(examId, position)).thenReturn(Optional.of(examItem));

        assertThat(examItemService.findExamItemAndResponse(examId, position).get()).isEqualTo(examItem);
    }
}
