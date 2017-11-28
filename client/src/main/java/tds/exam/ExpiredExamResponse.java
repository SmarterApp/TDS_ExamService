package tds.exam;

import java.util.Collection;

/**
 * Represents
 */
public class ExpiredExamResponse {
    private boolean additionalExamsToExpire;
    private Collection<ExpiredExamInformation> expiredExams;

    //For frameworks
    ExpiredExamResponse() {
    }

    public ExpiredExamResponse(final boolean additionalExamsToExpire, final Collection<ExpiredExamInformation> expiredExams) {
        this.additionalExamsToExpire = additionalExamsToExpire;
        this.expiredExams = expiredExams;
    }

    public boolean isAdditionalExamsToExpire() {
        return additionalExamsToExpire;
    }

    public Collection<ExpiredExamInformation> getExpiredExams() {
        return expiredExams;
    }
}
