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
    private String assessmentId;

    @NotNull
    private int maxOpportunities;

    @NotNull
    private UUID sessionId;

    @NotNull
    @Min(0)
    private int numberOfDaysToDelay;

    private Long proctorId;

    private String guestAccomodations;

    //TODO - find what this means
    public String getGuestAccomodations() {
        return guestAccomodations;
    }

    public void setGuestAccomodations(String guestAccomodations) {
        this.guestAccomodations = guestAccomodations;
    }

    /**
     * @return proctor id or null
     */
    public Long getProctorId() {
        return proctorId;
    }

    /**
     * @param proctorId proctor id if exam has a proctor
     */
    public void setProctorId(Long proctorId) {
        this.proctorId = proctorId;
    }

    /**
     * @return unique client name
     */
    public String getClientName() {
        return clientName;
    }

    /**
     * @param clientName unique client name
     */
    public void setClientName(String clientName) {
        this.clientName = clientName;
    }

    /**
     * @return identifier for the student taking the exam
     */
    public long getStudentId() {
        return studentId;
    }

    /**
     * @param studentId identifier for the student taking the exam
     */
    public void setStudentId(long studentId) {
        this.studentId = studentId;
    }

    /**
     * @return identifier for the assessment
     */
    public String getAssessmentId() {
        return assessmentId;
    }

    /**
     * @param assessmentId identifier for the assessment
     */
    public void setAssessmentId(String assessmentId) {
        this.assessmentId = assessmentId;
    }

    /**
     * @return max number of opportunities the student has to take the exam
     */
    public int getMaxOpportunities() {
        return maxOpportunities;
    }

    /**
     * @param maxOpportunities max number of opportunities the student has to take the exam
     */
    public void setMaxOpportunities(int maxOpportunities) {
        this.maxOpportunities = maxOpportunities;
    }

    /**
     * @return unique identifier for the session
     */
    public UUID getSessionId() {
        return sessionId;
    }

    /**
     * @param sessionId unique identifier for the session
     */
    public void setSessionId(UUID sessionId) {
        this.sessionId = sessionId;
    }

    /**
     * @return the number of days to delay retaking an exam
     */
    public int getNumberOfDaysToDelay() {
        return numberOfDaysToDelay;
    }

    /**
     * @param numberOfDaysToDelay the number of days to delay retaking an exam
     */
    public void setNumberOfDaysToDelay(int numberOfDaysToDelay) {
        this.numberOfDaysToDelay = numberOfDaysToDelay;
    }

    /**
     * @return {@code true} if the student is a guest
     */
    public boolean isGuestStudent() {
        return studentId <= 0;
    }
}
