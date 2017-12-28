package tds.exam.web.endpoints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Collections;
import java.util.UUID;

import tds.exam.ExpiredExamInformation;
import tds.exam.ExpiredExamResponse;
import tds.exam.services.ExamExpirationService;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamExpirationControllerTest {
    @Mock
    private ExamExpirationService mockExamExpirationService;

    private ExamExpirationController examExpirationController;

    @Before
    public void setUp() {
        examExpirationController = new ExamExpirationController(mockExamExpirationService);
    }

    @Test
    public void shouldExpireExams() {
        ExpiredExamInformation info = new ExpiredExamInformation(1L, "key", "id", UUID.randomUUID(), "status");
        when(mockExamExpirationService.expireExams("SBAC")).thenReturn(new ExpiredExamResponse(false, Collections.singletonList(info)));
        ResponseEntity<ExpiredExamResponse> response = examExpirationController.expireExams("SBAC");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().getExpiredExams()).containsExactly(info);
    }
}