package tds.exam.services.impl;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.exam.ApprovalRequest;
import tds.exam.ExamApproval;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.builder.ExamItemBuilder;
import tds.exam.builder.ExamPageBuilder;
import tds.exam.error.ValidationErrorCode;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamPageQueryRepository;
import tds.exam.services.ExamApprovalService;
import tds.exam.services.ExamPageService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamPageServiceImplTest {

    @Mock
    private ExamPageCommandRepository mockExamPageCommandRepository;

    @Mock
    private ExamPageQueryRepository mockExamPageQueryRepository;

    @Mock
    private ExamApprovalService mockExamApprovalService;

    private ExamPageService examPageService;

    @Before
    public void setUp() {
        examPageService = new ExamPageServiceImpl(mockExamPageQueryRepository,
            mockExamPageCommandRepository,
            mockExamApprovalService);
    }

    @Test
    public void shouldReturnAllPagesForExam() {
        final UUID examId = UUID.randomUUID();
        ExamPage examPage1 = new ExamPageBuilder()
            .build();
        ExamPage examPage2 = new ExamPageBuilder()
            .withId(UUID.randomUUID())
            .build();
        List<ExamPage> examPages = new ArrayList<>();
        examPages.add(examPage1);
        examPages.add(examPage2);

        when(mockExamPageQueryRepository.findAll(examId)).thenReturn(examPages);
        List<ExamPage> retExamPages = examPageService.findAllPages(examId);
        assertThat(retExamPages).hasSize(2);
    }

    @Test
    public void shouldDeletePagesForExamId() {
        final UUID examId = UUID.randomUUID();
        examPageService.deletePages(examId);
        verify(mockExamPageCommandRepository).deleteAll(examId);
    }

    @Test
    public void shouldInsertPagesForExamId() {
        ExamPage examPage1 = new ExamPageBuilder()
            .build();

        examPageService.insertPages(examPage1);
        verify(mockExamPageCommandRepository).insert(examPage1);
    }

    @Test
    public void shouldGetAnExamPageWithItems() {
        UUID mockExamId = UUID.randomUUID();
        UUID mockSessionId = UUID.randomUUID();
        UUID mockBrowserId = UUID.randomUUID();
        String mockClientName = "UNIT_TEST";

        Instant respondedAtInstant = Instant.now().minus(200000);
        ExamItem mockFirstExamItem = new ExamItemBuilder()
            .withExamPageId(ExamPageBuilder.DEFAULT_ID)
            .withItemKey("187-1234")
            .withRequired(true)
            .withResponse(new ExamItemResponse.Builder()
                .withResponse("first item response")
                .withValid(true)
                .withCreatedAt(respondedAtInstant)
                .build())
            .build();
        ExamItem mockSecondExamItem = new ExamItemBuilder()
            .withId(UUID.randomUUID())
            .withExamPageId(ExamPageBuilder.DEFAULT_ID)
            .withItemKey("187-5678")
            .withAssessmentItemKey(5678L)
            .withItemType("TEST")
            .withItemFilePath("/path/to/item/187-5678.xml")
            .build();

        List<ExamItem> mockExamItems = Arrays.asList(mockFirstExamItem, mockSecondExamItem);

        ExamPage mockExamPage = new ExamPageBuilder()
            .withExamId(mockExamId)
            .withExamItems(mockExamItems)
            .build();

        ApprovalRequest mockApprovalRequest = new ApprovalRequest(mockExamId,
            mockSessionId,
            mockBrowserId,
            mockClientName);

        ExamApproval mockExamApproval = new ExamApproval(mockExamId,
            new ExamStatusCode(ExamStatusCode.STATUS_STARTED, ExamStatusStage.IN_PROGRESS),
            null);

        when(mockExamApprovalService.getApproval(mockApprovalRequest))
            .thenReturn(new Response<>(mockExamApproval));
        when(mockExamPageQueryRepository.findPageWithItems(mockExamId, mockExamPage.getPagePosition()))
            .thenReturn(Optional.of(mockExamPage));

        Response<ExamPage> examPageResponse = examPageService.getPage(mockApprovalRequest, mockExamPage.getPagePosition());
        verify(mockExamApprovalService).getApproval(mockApprovalRequest);
        verify(mockExamPageQueryRepository).findPageWithItems(mockExamPage.getExamId(), mockExamPage.getPagePosition());
        verify(mockExamPageCommandRepository).update(any(ExamPage[].class));

        assertThat(examPageResponse.getData().isPresent()).isTrue();
        assertThat(examPageResponse.hasError()).isFalse();

        ExamPage examPage = examPageResponse.getData().get();
        assertThat(examPage).isEqualToComparingFieldByFieldRecursively(mockExamPage);
    }

    @Test
    public void shouldNotGetAnExamPageWhenTheApprovalRequestIsDeniedBecauseSessionIsClosed() {
        UUID mockExamId = UUID.randomUUID();
        UUID mockSessionId = UUID.randomUUID();
        UUID mockBrowserId = UUID.randomUUID();
        String mockClientName = "UNIT_TEST";
        ApprovalRequest mockApprovalRequest = new ApprovalRequest(mockExamId,
            mockSessionId,
            mockBrowserId,
            mockClientName);

        Response<ExamApproval> mockApprovalFailure = new Response<>(new ValidationError(ValidationErrorCode.EXAM_APPROVAL_SESSION_CLOSED, "The session is not available for testing, please check with your test administrator."));

        when(mockExamApprovalService.getApproval(mockApprovalRequest))
            .thenReturn(mockApprovalFailure);

        Response<ExamPage> examPageResponse = examPageService.getPage(mockApprovalRequest, 1);
        verify(mockExamApprovalService).getApproval(mockApprovalRequest);
        verifyZeroInteractions(mockExamPageQueryRepository);

        assertThat(examPageResponse.getError().isPresent()).isTrue();
        assertThat(examPageResponse.getData().isPresent()).isFalse();
    }
}
