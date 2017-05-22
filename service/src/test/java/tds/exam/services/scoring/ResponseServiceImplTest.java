package tds.exam.services.scoring;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.joda.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import tds.exam.Exam;
import tds.exam.ExamItem;
import tds.exam.ExamItemResponse;
import tds.exam.ExamItemResponseScore;
import tds.exam.ExamPage;
import tds.exam.ExamScoringStatus;
import tds.exam.ExamStatusCode;
import tds.exam.builder.ExamBuilder;
import tds.exam.repositories.ExamItemCommandRepository;
import tds.exam.repositories.ExamItemQueryRepository;
import tds.exam.services.ConfigService;
import tds.exam.services.ExamPageService;
import tds.exam.services.ExamService;
import tds.score.model.ExamInstance;
import tds.score.services.ResponseService;
import tds.student.sql.data.IItemResponseUpdate;
import tds.student.sql.data.ItemResponseUpdate;

import java.util.Optional;
import java.util.UUID;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static tds.exam.services.scoring.ResponseServiceImpl.VALID_EXAM_STATUS_CODES;

@RunWith(MockitoJUnitRunner.class)
public class ResponseServiceImplTest {
    @Mock
    private ExamService mockExamService;

    @Mock
    private ConfigService mockConfigService;

    @Mock
    private ExamItemCommandRepository mockExamItemCommandRepository;

    @Mock
    private ExamItemQueryRepository mockExamItemQueryRepository;

    @Mock
    private ExamPageService mockExamPageService;

    private ResponseService responseService;

    @Before
    public void setUp() {
        responseService = new ResponseServiceImpl(mockExamService,
            mockConfigService,
            mockExamItemCommandRepository,
            mockExamItemQueryRepository,
            mockExamPageService);
    }

    @After
    public void tearDown() {
    }

    @Test (expected = ReturnStatusException.class)
    public void itShouldThrowWhenExamCannotBeFound() throws ReturnStatusException {
        UUID examId = UUID.randomUUID();
        ExamInstance examInstance = ExamInstance.create(examId, UUID.randomUUID(), UUID.randomUUID(), "SBAC_PT");
        IItemResponseUpdate update = random(ItemResponseUpdate.class);
        when(mockExamService.findExam(examId)).thenReturn(Optional.empty());
        responseService.updateScoredResponse(examInstance, update, 1, ExamScoringStatus.SCORED.toString(), "rationale", 1, 200);
    }

    @Test (expected = ReturnStatusException.class)
    public void itShouldThrowIfExamNotInCorrectStatus() throws ReturnStatusException {
        Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PAUSED), Instant.now())
            .build();
        ExamInstance examInstance = ExamInstance.create(exam.getId(), UUID.randomUUID(), UUID.randomUUID(), "SBAC_PT");
        IItemResponseUpdate update = random(ItemResponseUpdate.class);
        when(mockExamService.findExam(exam.getId())).thenReturn(Optional.of(exam));
        responseService.updateScoredResponse(examInstance, update, 1, ExamScoringStatus.SCORED.toString(), "rationale", 1, 200);
    }

    @Test
    public void itShouldSaveAPositiveScore() throws Exception {
        final ExamInstance examInstance = ExamInstance.create(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), "clientName");

        final ItemResponseUpdate itemResponseUpdate = random(ItemResponseUpdate.class);
        itemResponseUpdate.setDateCreated(null);
        itemResponseUpdate.setSequence(1);

        final ExamStatusCode validStatus = new ExamStatusCode(VALID_EXAM_STATUS_CODES.iterator().next());
        final Exam exam = new Exam.Builder()
            .fromExam(random(Exam.class))
            .withStatus(validStatus, Instant.now())
            .build();
        when(mockExamService.findExam(examInstance.getExamId())).thenReturn(Optional.of(exam));

        final ExamItemResponse examItemResponse = new ExamItemResponse.Builder()
            .withScore(new ExamItemResponseScore.Builder().build())
            .withCreatedAt(Instant.now())
            .withExamItemId(UUID.randomUUID())
            .withExamId(UUID.randomUUID())
            .withResponse("Response")
            .withValid(true)
            .withSequence(1)
            .build();
        final ExamItem examItem = new ExamItem.Builder(examItemResponse.getExamItemId())
            .withResponse(examItemResponse)
            .withGroupId("groupId")
            .withCreatedAt(Instant.now())
            .withStimulusFilePath("stimulus")
            .withAssessmentItemBankKey(123)
            .withAssessmentItemKey(456)
            .withStimulusFilePath("stimulus")
            .withItemFilePath("item")
            .withExamPageId(UUID.randomUUID())
            .withItemKey(itemResponseUpdate.getItemID())
            .withItemType("itemType")
            .withPosition(1)
            .build();
        when(mockExamItemQueryRepository.findExamItemAndResponse(examInstance.getExamId(), itemResponseUpdate.getPosition()))
            .thenReturn(Optional.of(examItem));

        final ExamPage examPage = random(ExamPage.class);
        when(mockExamPageService.find(examItem.getExamPageId())).thenReturn(Optional.of(examPage));

        responseService.updateScoredResponse(examInstance, itemResponseUpdate,
            1, "Scored", "scoreRationale", 123L, 456L);

        final ArgumentCaptor<ExamItemResponse> responseCaptor = ArgumentCaptor.forClass(ExamItemResponse.class);
        verify(mockExamItemCommandRepository).insertResponses(responseCaptor.capture());

        final ExamItemResponse persisted = responseCaptor.getValue();
        assertThat(persisted.getScore().get().getScore()).isEqualTo(1);
    }
}