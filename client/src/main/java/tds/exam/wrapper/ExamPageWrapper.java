package tds.exam.wrapper;

import java.util.List;

import tds.exam.ExamItem;
import tds.exam.ExamPage;

public class ExamPageWrapper {
    private final ExamPage examPage;
    private final List<ExamItem> examItems;

    public ExamPageWrapper(final ExamPage examPage, final List<ExamItem> examItems) {
        this.examPage = examPage;
        this.examItems = examItems;
    }

    public ExamPage getExamPage() {
        return examPage;
    }

    public List<ExamItem> getExamItems() {
        return examItems;
    }
}
