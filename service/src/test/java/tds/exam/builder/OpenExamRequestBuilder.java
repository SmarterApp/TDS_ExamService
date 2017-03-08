package tds.exam.builder;

import java.util.UUID;

import tds.exam.OpenExamRequest;

public class OpenExamRequestBuilder {
    private long studentId = 1;
    private String assessmentKey = "(SBAC_PT)IRP-Perf-ELA-3-Summer-2015-2016";
    private int maxAttempts = 0;
    private UUID sessionId = UUID.randomUUID();
    private String guestAccommodations;
    private UUID browserId = UUID.randomUUID();

    public OpenExamRequestBuilder withStudentId(long studentId) {
        this.studentId = studentId;
        return this;
    }

    public OpenExamRequestBuilder withAssessmentKey(String assessmentKey) {
        this.assessmentKey = assessmentKey;
        return this;
    }

    public OpenExamRequestBuilder withMaxAttempts(int maxAttempts) {
        this.maxAttempts = maxAttempts;
        return this;
    }

    public OpenExamRequestBuilder withSessionId(UUID sessionId) {
        this.sessionId = sessionId;
        return this;
    }

    public OpenExamRequestBuilder withGuestAccommodations(String guestAccommodations) {
        this.guestAccommodations = guestAccommodations;
        return this;
    }

    public OpenExamRequestBuilder withBrowserId(UUID browserId) {
        this.browserId = browserId;
        return this;
    }

    public OpenExamRequest build() {
        return new OpenExamRequest.Builder()
            .withStudentId(studentId)
            .withAssessmentKey(assessmentKey)
            .withMaxAttempts(maxAttempts)
            .withSessionId(sessionId)
            .withGuestAccommodations(guestAccommodations)
            .withBrowserId(browserId)
            .build();
    }
}
