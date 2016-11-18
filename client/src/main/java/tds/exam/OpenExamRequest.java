package tds.exam;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Information required to open an exam
 */
public class OpenExamRequest {
    @NotNull
    private String clientName;

    @NotNull
    private long studentId;

    @NotNull
    private String assessmentKey;

    @NotNull
    private int maxAttempts;

    @NotNull
    private UUID sessionId;

    //TODO - Delete because now we have to fetch this
    @NotNull
    @Min(0)
    private int numberOfDaysToDelay;

    /**
     * TODO - This should be fetched from session instead of passed in
     */
    private Long proctorId;

    private String guestAccommodations;

    private UUID browserId;

    private OpenExamRequest(Builder builder) {
        this.clientName = builder.clientName;
        this.studentId = builder.studentId;
        this.assessmentKey = builder.assessmentKey;
        this.maxAttempts = builder.maxAttempts;
        this.sessionId = builder.sessionId;
        this.numberOfDaysToDelay = builder.numberOfDaysToDelay;
        this.proctorId = builder.proctorId;
        this.guestAccommodations = builder.guestAccommodations;
        this.browserId = builder.browserId;
    }

    /**
     * @return accommodations that are needed when a guest is taking the exam
     */
    public String getGuestAccommodations() {
        return guestAccommodations;
    }

    /**
     * @return proctor id or null
     */
    public Long getProctorId() {
        return proctorId;
    }

    /**
     * @return unique client name
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * @return identifier for the student taking the exam
     */
    public long getStudentId() {
        return studentId;
    }

    /**
     * @return identifier for the assessment
     */
    public String getAssessmentKey() {
        return assessmentKey;
    }


    //TODO I guess exam should fetch this via client_testproperty
    /**
     * @return max number of attempts the student has to take the exam
     */
    public Integer getMaxAttempts() {
        return maxAttempts;
    }

    /**
     * @return unique identifier for the session
     */
    public UUID getSessionId() {
        return sessionId;
    }

    /**
     * @return the number of days to delay retaking an exam
     */
    public int getNumberOfDaysToDelay() {
        return numberOfDaysToDelay;
    }

    /**
     * @return {@code true} if the student is a guest
     */
    public boolean isGuestStudent() {
        return studentId <= 0;
    }

    public UUID getBrowserId() {
        return browserId;
    }

    public static final class Builder {
        private String clientName;
        private long studentId;
        private String assessmentKey;
        private int maxAttempts;
        private UUID sessionId;
        private int numberOfDaysToDelay;
        private Long proctorId;
        private String guestAccommodations;
        private UUID browserId;

        public Builder withClientName(String clientName) {
            this.clientName = clientName;
            return this;
        }

        public Builder withStudentId(long studentId) {
            this.studentId = studentId;
            return this;
        }

        public Builder withAssessmentKey(String assessmentKey) {
            this.assessmentKey = assessmentKey;
            return this;
        }

        public Builder withMaxAttempts(int maxAttempts) {
            this.maxAttempts = maxAttempts;
            return this;
        }

        public Builder withSessionId(UUID sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder withNumberOfDaysToDelay(int numberOfDaysToDelay) {
            this.numberOfDaysToDelay = numberOfDaysToDelay;
            return this;
        }

        public Builder withProctorId(Long proctorId) {
            this.proctorId = proctorId;
            return this;
        }

        public Builder withGuestAccommodations(String guestAccommodations) {
            this.guestAccommodations = guestAccommodations;
            return this;
        }

        public Builder withBrowserId(UUID browserId) {
            this.browserId = browserId;
            return this;
        }

        public OpenExamRequest build() {
            return new OpenExamRequest(this);
        }
    }
}
