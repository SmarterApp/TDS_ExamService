package tds.exam;

public class ExpiredExamInformation {
    private long studentId;
    private String assessmentKey;
    private String assessmentId;
    private String examId;

    //For frameworks
    ExpiredExamInformation() {
    }

    public ExpiredExamInformation(final long studentId, final String assessmentKey, final String assessmentId, final String examId) {
        this.studentId = studentId;
        this.assessmentKey = assessmentKey;
        this.assessmentId = assessmentId;
        this.examId = examId;
    }

    public long getStudentId() {
        return studentId;
    }

    public String getAssessmentKey() {
        return assessmentKey;
    }

    public String getAssessmentId() {
        return assessmentId;
    }

    public String getExamId() {
        return examId;
    }
}
