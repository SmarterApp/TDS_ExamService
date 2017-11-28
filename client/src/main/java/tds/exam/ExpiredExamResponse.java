package tds.exam;

import java.util.Collection;

/**
 * Contains information about the exams that were expired and also whether there are additional exams that need to be expired.
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

    /**
     * @return {@code true} if there are more exams to expire
     */
    public boolean isAdditionalExamsToExpire() {
        return additionalExamsToExpire;
    }

    /**
     * @return the collection of {@link tds.exam.ExpiredExamInformation} for expired exams
     */
    public Collection<ExpiredExamInformation> getExpiredExams() {
        return expiredExams;
    }
}
