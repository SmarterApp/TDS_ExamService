package tds.exam.services.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import tds.exam.ExamPrintRequest;
import tds.exam.repositories.ExamPrintRequestCommandRepository;
import tds.exam.repositories.ExamPrintRequestQueryRepository;
import tds.exam.services.ExamPrintRequestService;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ExamPrintRequestServiceImplTest {
    private ExamPrintRequestService examPrintRequestService;

    @Mock
    private ExamPrintRequestQueryRepository mockExamPrintRequestQueryRepository;

    @Mock
    private ExamPrintRequestCommandRepository mockExamPrintRequestCommandRepository;

    @Before
    public void setup() {
        examPrintRequestService = new ExamPrintRequestServiceImpl(mockExamPrintRequestCommandRepository,
            mockExamPrintRequestQueryRepository);
    }

    @Test
    public void shouldCreateExamPrintRequest() {
        ExamPrintRequest examPrintRequest = random(ExamPrintRequest.class);
        examPrintRequestService.insert(examPrintRequest);
        verify(mockExamPrintRequestCommandRepository).insert(examPrintRequest);
    }
}
