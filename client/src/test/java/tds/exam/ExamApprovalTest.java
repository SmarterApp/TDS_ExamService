package tds.exam;

import org.junit.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ExamApprovalTest {
    @Test
    public void shouldCreateAnExamApproval() {
        UUID mockExamId = UUID.randomUUID();
        ExamStatusCode mockExamStatusCode = new ExamStatusCode.Builder()
                .withStatus("open")
                .build();
        ExamApproval examApproval = new ExamApproval(mockExamId, mockExamStatusCode, "unit test");

        assertThat(examApproval.getExamId()).isEqualTo(mockExamId);
        assertThat(examApproval.getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.WAITING);
        assertThat(examApproval.getStatusChangeReason()).isEqualTo("unit test");
    }

    @Test
    public void shouldCreateAnExamApprovalWithApprovedStatus() {
        UUID mockExamId = UUID.randomUUID();
        ExamStatusCode mockExamStatusCode = new ExamStatusCode.Builder()
                .withStatus("approved")
                .build();
        ExamApproval examApproval = new ExamApproval(mockExamId, mockExamStatusCode, "unit test");

        assertThat(examApproval.getExamId()).isEqualTo(mockExamId);
        assertThat(examApproval.getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.APPROVED);
        assertThat(examApproval.getStatusChangeReason()).isEqualTo("unit test");
    }

    @Test
    public void shouldCreateAnExamApprovalWithDeniedStatus() {
        UUID mockExamId = UUID.randomUUID();
        ExamStatusCode mockExamStatusCode = new ExamStatusCode.Builder()
                .withStatus("denied")
                .build();
        ExamApproval examApproval = new ExamApproval(mockExamId, mockExamStatusCode, "unit test");

        assertThat(examApproval.getExamId()).isEqualTo(mockExamId);
        assertThat(examApproval.getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.DENIED);
        assertThat(examApproval.getStatusChangeReason()).isEqualTo("unit test");
    }

    @Test
    public void shouldCreateAnExamApprovalWithLogoutStatus() {
        UUID mockExamId = UUID.randomUUID();
        ExamStatusCode mockExamStatusCode = new ExamStatusCode.Builder()
                .withStatus("paused")
                .build();
        ExamApproval examApproval = new ExamApproval(mockExamId, mockExamStatusCode, "unit test");

        assertThat(examApproval.getExamId()).isEqualTo(mockExamId);
        assertThat(examApproval.getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.LOGOUT);
        assertThat(examApproval.getStatusChangeReason()).isEqualTo("unit test");
    }

    @Test
    public void shouldCreateExamApprovalWithWaitingStatusWhenExamStatusIsNotRecognized() {
        UUID mockExamId = UUID.randomUUID();
        ExamStatusCode mockExamStatusCode = new ExamStatusCode.Builder()
                .withStatus("foo")
                .build();
        ExamApproval examApproval = new ExamApproval(mockExamId, mockExamStatusCode, "unit test");

        assertThat(examApproval.getExamId()).isEqualTo(mockExamId);
        assertThat(examApproval.getExamApprovalStatus()).isEqualTo(ExamApprovalStatus.WAITING);
        assertThat(examApproval.getStatusChangeReason()).isEqualTo("unit test");
    }

    @Test(expected=IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionWhenStatusIsNull() {
        UUID mockExamId = UUID.randomUUID();
        ExamStatusCode mockExamStatusCode = new ExamStatusCode.Builder()
                .withStatus(null)
                .build();

        new ExamApproval(mockExamId, mockExamStatusCode, "unit test");
    }
}
