package tds.exam;

import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static tds.exam.ExamStatusStage.CLOSED;
import static tds.exam.ExamStatusStage.INUSE;
import static tds.exam.ExamStatusStage.OPEN;

public class ExamApprovalTest {
    @Test
    public void shouldCreateAnExamApproval() {
        UUID mockExamId = UUID.randomUUID();
        ExamStatusCode mockExamStatusCode = new ExamStatusCode("open", OPEN);
        ExamApproval examApproval = new ExamApproval(mockExamId, mockExamStatusCode, "unit test");

        assertThat(examApproval.getExamId()).isEqualTo(mockExamId);
        assertThat(examApproval.getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.WAITING);
        assertThat(examApproval.getStatusChangeReason()).isEqualTo("unit test");
    }

    @Test
    public void shouldCreateAnExamApprovalWithApprovedStatus() {
        UUID mockExamId = UUID.randomUUID();
        ExamStatusCode mockExamStatusCode = new ExamStatusCode("approved", OPEN);
        ExamApproval examApproval = new ExamApproval(mockExamId, mockExamStatusCode, "unit test");

        assertThat(examApproval.getExamId()).isEqualTo(mockExamId);
        assertThat(examApproval.getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.APPROVED);
        assertThat(examApproval.getStatusChangeReason()).isEqualTo("unit test");
    }

    @Test
    public void shouldCreateAnExamApprovalWithDeniedStatus() {
        UUID mockExamId = UUID.randomUUID();
        ExamStatusCode mockExamStatusCode = new ExamStatusCode("denied", CLOSED);
        ExamApproval examApproval = new ExamApproval(mockExamId, mockExamStatusCode, "unit test");

        assertThat(examApproval.getExamId()).isEqualTo(mockExamId);
        assertThat(examApproval.getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.DENIED);
        assertThat(examApproval.getStatusChangeReason()).isEqualTo("unit test");
    }

    @Test
    public void shouldCreateAnExamApprovalWithLogoutStatus() {
        UUID mockExamId = UUID.randomUUID();
        ExamStatusCode mockExamStatusCode = new ExamStatusCode("paused", INUSE);
        ExamApproval examApproval = new ExamApproval(mockExamId, mockExamStatusCode, "unit test");

        assertThat(examApproval.getExamId()).isEqualTo(mockExamId);
        assertThat(examApproval.getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.LOGOUT);
        assertThat(examApproval.getStatusChangeReason()).isEqualTo("unit test");
    }

    @Test
    public void shouldCreateExamApprovalWithWaitingStatusWhenExamStatusIsNotRecognized() {
        UUID mockExamId = UUID.randomUUID();
        ExamStatusCode mockExamStatusCode = new ExamStatusCode("foo", CLOSED);
        ExamApproval examApproval = new ExamApproval(mockExamId, mockExamStatusCode, "unit test");

        assertThat(examApproval.getExamId()).isEqualTo(mockExamId);
        assertThat(examApproval.getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.WAITING);
        assertThat(examApproval.getStatusChangeReason()).isEqualTo("unit test");
    }

    @Test(expected=IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenStatusIsNull() {
        UUID mockExamId = UUID.randomUUID();
        ExamStatusCode mockExamStatusCode = new ExamStatusCode(null, null);

        new ExamApproval(mockExamId, mockExamStatusCode, "unit test");
    }
}
