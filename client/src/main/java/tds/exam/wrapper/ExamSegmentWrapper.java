package tds.exam.wrapper;

import java.util.List;

import tds.exam.ExamSegment;

public class ExamSegmentWrapper {
    private ExamSegment examSegment;
    private List<ExamPageWrapper> examPages;

    public ExamSegmentWrapper(final ExamSegment examSegment, final List<ExamPageWrapper> examPages) {
        this.examSegment = examSegment;
        this.examPages = examPages;
    }

    public ExamSegment getExamSegment() {
        return examSegment;
    }

    public List<ExamPageWrapper> getExamPages() {
        return examPages;
    }
}

