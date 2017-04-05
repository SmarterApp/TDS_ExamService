package tds.exam.services.scoring;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.joda.time.Instant;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;
import java.util.UUID;

import tds.exam.Exam;
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

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.mockito.Mockito.when;

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
}