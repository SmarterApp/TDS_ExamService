package tds.exam.messaging;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.UUID;

import tds.exam.ExamStatusCode;
import tds.exam.services.ExamService;

import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class ExamReportedMessageListenerTest {
    private ExamReportedMessageListener listener;

    @Mock
    public ExamService mockExamService;

    @Before
    public void setUp() {
        listener = new ExamReportedMessageListener(mockExamService);
    }

    @Test
    public void shouldUpdateExamStatusToReported() {
        final UUID examId = UUID.randomUUID();
        listener.handleMessage(examId.toString());
        verify(mockExamService).updateExamStatus(examId, new ExamStatusCode(ExamStatusCode.STATUS_REPORTED));
    }
}
