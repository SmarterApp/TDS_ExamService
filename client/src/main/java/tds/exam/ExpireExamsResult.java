package tds.exam;

import java.util.Set;

public class ExpireExamsResult {
    private Set<ExpiredExamInformation> expiredExams;
    private Set<ExpiredExamInformation> failedExpiredExams;

    // For frameworks
    ExpireExamsResult() {}

    public Set<ExpiredExamInformation> getExpiredExams() {
        return expiredExams;
    }

    public Set<ExpiredExamInformation> getFailedExpiredExams() {
        return failedExpiredExams;
    }
}
