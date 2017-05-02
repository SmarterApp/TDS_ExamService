package tds.exam.wrapper;

import java.util.List;

import tds.exam.ExamSegment;

/**
 * Contains the {@link tds.exam.ExamSegment} and its associated {@link tds.exam.ExamPage}s
 */
public class ExamSegmentWrapper {
    private ExamSegment examSegment;
    private List<ExamPageWrapper> examPages;

    public ExamSegmentWrapper(final ExamSegment examSegment, final List<ExamPageWrapper> examPages) {
        this.examSegment = examSegment;
        this.examPages = examPages;
    }

    //For frameworks
    private ExamSegmentWrapper() {
    }

    /**
     * @return the {@link tds.exam.ExamSegment}
     */
    public ExamSegment getExamSegment() {
        return examSegment;
    }

    /**
     * @return the {@link tds.exam.ExamPage}s associated with the {@link tds.exam.ExamSegment}
     */
    public List<ExamPageWrapper> getExamPages() {
        return examPages;
    }
}

