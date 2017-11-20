package tds.exam;

import java.util.UUID;

/**
 * Information on the expired exam
 */
public class ExpiredExamInformation {
    private long studentId;
    private String assessmentKey;
    private String assessmentId;
    private UUID examId;

    //For frameworks
    ExpiredExamInformation() {
    }

    public ExpiredExamInformation(final long studentId, final String assessmentKey, final String assessmentId, final UUID examId) {
        this.studentId = studentId;
        this.assessmentKey = assessmentKey;
        this.assessmentId = assessmentId;
        this.examId = examId;
    }

    /**
     * @return the student id associated with the exam
     */
    public long getStudentId() {
        return studentId;
    }

    /**
     * @return the assessment key
     */
    public String getAssessmentKey() {
        return assessmentKey;
    }

    /**
     * @return the assessment id
     */
    public String getAssessmentId() {
        return assessmentId;
    }

    /**
     * @return the exam id
     */
    public UUID getExamId() {
        return examId;
    }
}
