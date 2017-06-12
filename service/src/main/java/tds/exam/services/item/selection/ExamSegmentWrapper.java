package tds.exam.services.item.selection;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import tds.exam.ExamItem;
import tds.exam.ExamPage;
import tds.exam.ExamSegment;

/**
 * A Wrapper class that associates {@link tds.exam.ExamSegment} with its exams
 */
class ExamSegmentWrapper {
    private ExamSegment examSegment;
    private Set<UUID> pageIds = new HashSet<>();
    private List<ExamItem> items = new ArrayList<>();

    public ExamSegmentWrapper(final ExamSegment examSegment) {
        this.examSegment = examSegment;
    }

    /**
     * @param pageId adds a page id to the list of pages available for an exam
     */
    public void addPageId(final UUID pageId) {
        pageIds.add(pageId);
    }

    /**
     * @param item adds an item to the list of items
     */
    public void addItem(final ExamItem item) {
        items.add(item);
    }

    /**
     * @param examPage the exam page
     * @return {@code true} if the page is within the exam segment
     */
    public boolean isPageInSegment(final ExamPage examPage) {
        return examSegment.getSegmentKey().equals(examPage.getSegmentKey())
            && examSegment.getExamId().equals(examPage.getExamId());
    }

    /**
     * @param examItem the {@link tds.exam.ExamItem}
     * @return {@code true} if the exam item has a page id within the exam segment
     */
    public boolean isItemInSegment(final ExamItem examItem) {
        return pageIds.contains(examItem.getExamPageId());
    }

    /**
     * @return the list of {@link tds.exam.ExamItem} associated with the Exam segment
     */
    public List<ExamItem> getItems() {
        return items;
    }

    /**
     * @return the exam segment
     */
    public ExamSegment getExamSegment() {
        return examSegment;
    }

    public void setExamSegment(final ExamSegment examSegment) {
        this.examSegment = examSegment;
    }
}
