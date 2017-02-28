package tds.exam;

import java.util.List;

/**
 * A model representing an {@link tds.exam.Exam} as well as other optional exam-specific data
 */
public class ExpandableExam {
    public static final String EXPANDABLE_PARAMS_EXAM_ACCOMMODATIONS = "examAccommodations";
    public static final String EXPANDABLE_PARAMS_ITEM_RESPONSE_COUNT = "itemsResponseCount";
    public static final String EXPANDABLE_PARAMS_UNFULFILLED_REQUEST_COUNT = "unfulfilledRequestCount";

    private Exam exam;
    private int itemsResponseCount;
    private int requestCount;
    private List<ExamAccommodation> examAccommodations;
    private boolean multiStageBraille;

    /* Empty private constructor for frameworks */
    private ExpandableExam() {}

    public ExpandableExam(Builder builder) {
        this.exam = builder.exam;
        this.examAccommodations = builder.examAccommodations;
        this.itemsResponseCount = builder.itemsResponseCount;
        this.requestCount = builder.requestCount;
        this.multiStageBraille = builder.multiStageBraille;
    }

    public static class Builder {
        private Exam exam;
        private List<ExamAccommodation> examAccommodations;
        private int itemsResponseCount;
        private int requestCount;
        private boolean multiStageBraille;

        public Builder(Exam exam) {
            this.exam = exam;
        }

        public Builder withExamAccommodations(List<ExamAccommodation> examAccommodations) {
            this.examAccommodations = examAccommodations;
            return this;
        }

        public Builder withItemsResponseCount(int itemsResponseCount) {
            this.itemsResponseCount = itemsResponseCount;
            return this;
        }

        public Builder withRequestCount(int requestCount) {
            this.requestCount = requestCount;
            return this;
        }

        public Builder withMultiStageBraille(boolean multiStageBraille) {
            this.multiStageBraille = multiStageBraille;
            return this;
        }

        public ExpandableExam build() {
            return new ExpandableExam(this);
        }
    }

    /**
     * @return The base {@link tds.exam.Exam}
     */
    public Exam getExam() {
        return exam;
    }

    /**
     * @return The {@link tds.exam.ExamAccommodation}s of the exam
     */
    public List<ExamAccommodation> getExamAccommodations() {
        return examAccommodations;
    }

    /**
     * @return The count of items that have existing responses for the exam
     */
    public int getItemsResponseCount() {
        return itemsResponseCount;
    }

    /**
     * @return The number of unfulfilled print or emboss requests submitted
     */
    public int getRequestCount() {
        return requestCount;
    }

    /**
     * @return Flag indicating that the exam contains multi stage braille segments
     */
    public boolean isMultiStageBraille() {
        return multiStageBraille;
    }
}
