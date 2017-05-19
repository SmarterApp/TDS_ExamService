package tds.exam.wrapper;

import java.util.ArrayList;
import java.util.List;

import tds.exam.ExamItem;
import tds.exam.ExamPage;

/**
 * Contains the {@link tds.exam.ExamPage} and its associated {@link tds.exam.ExamItem}s
 */
public class ExamPageWrapper {
    private ExamPage examPage;
    private List<ExamItem> examItems = new ArrayList<>();
    private boolean visible;

    public ExamPageWrapper(final ExamPage examPage, final List<ExamItem> examItems, final boolean visible) {
        this.examPage = examPage;
        this.examItems = examItems;
        this.visible = visible;
    }

    //For frameworks
    private ExamPageWrapper() {
    }

    /**
     * @return the {@link tds.exam.ExamPage}
     */
    public ExamPage getExamPage() {
        return examPage;
    }

    /**
     * @return the associated {@link tds.exam.ExamPage}'s {@link tds.exam.ExamItem}
     */
    public List<ExamItem> getExamItems() {
        return examItems;
    }

    public boolean isVisible() {
        return visible;
    }
}
