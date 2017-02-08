package tds.exam;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Information required to open an exam
 */
public class OpenExamRequest {

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

    private String guestAccommodations;

    private UUID browserId;

    private OpenExamRequest(Builder builder) {
        this.studentId = builder.studentId;
        this.assessmentKey = builder.assessmentKey;
        this.maxAttempts = builder.maxAttempts;
        this.sessionId = builder.sessionId;
        this.numberOfDaysToDelay = builder.numberOfDaysToDelay;
        this.guestAccommodations = builder.guestAccommodations;
        this.browserId = builder.browserId;
    }

    /**
     * Private constructor for frameworks
     */
    private OpenExamRequest() {
    }

    /**
     * @return accommodations that are needed when a guest is taking the exam
     */
    public String getGuestAccommodations() {
        return guestAccommodations;
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
        private long studentId;
        private String assessmentKey;
        private int maxAttempts;
        private UUID sessionId;
        private int numberOfDaysToDelay;
        private String guestAccommodations;
        private UUID browserId;

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

    @Override
    public String toString() {
        return "OpenExamRequest{" +
            "studentId=" + studentId +
            ", assessmentKey='" + assessmentKey + '\'' +
            ", maxAttempts=" + maxAttempts +
            ", sessionId=" + sessionId +
            ", numberOfDaysToDelay=" + numberOfDaysToDelay +
            ", guestAccommodations='" + guestAccommodations + '\'' +
            ", browserId=" + browserId +
            '}';
    }
}
