package tds.exam.services.scoring;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import tds.exam.repositories.ExamItemCommandRepository;
import tds.exam.repositories.ExamItemQueryRepository;
import tds.exam.services.ConfigService;
import tds.exam.services.ExamPageService;
import tds.exam.services.ExamService;
import tds.score.model.ExamInstance;
import tds.score.services.ResponseService;

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

    @Test
    public void itShouldThrowWhenExamCannotBeFound() {
        UUID examId = UUID.randomUUID();
        ExamInstance examInstance = ExamInstance.create(examId, UUID.randomUUID(), UUID.randomUUID(), "SBAC_PT");
//        IItemResponseUpdate update = new ItemResponseUpdate("testKey", "testId", "language", )
//        responseService.updateScoredResponse(examInstance, )
    }

}