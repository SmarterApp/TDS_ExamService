package tds.exam;

import java.util.List;

/**
 * A model representing an {@link tds.exam.Exam} as well as other optional exam-specific data
 */
public class ExpandableExam {
    public static final String EXPANDABLE_PARAMS_EXAM_ACCOMMODATIONS = "examAccommodations";
    public static final String EXPANDABLE_PARAMS_ITEM_RESPONSE_COUNT = "itemResponseCount";
    public static final String EXPANDABLE_PARAMS_UNFULFILLED_REQUEST_COUNT = "unfulfilledRequestCount";

    private Exam exam;
    private int itemResponseCount;
    private List<ExamAccommodation> examAccommodations;

    /* Empty private constructor for frameworks */
    private ExpandableExam() {}

    public ExpandableExam(Builder builder) {
        this.exam = builder.exam;
        this.examAccommodations = builder.examAccommodations;
        this.itemResponseCount = builder.itemResponseCount;
    }

    public static class Builder {
        private Exam exam;
        private List<ExamAccommodation> examAccommodations;
        private int itemResponseCount;

        public Builder(Exam exam) {
            this.exam = exam;
        }

        public Builder withExamAccommodations(List<ExamAccommodation> examAccommodations) {
            this.examAccommodations = examAccommodations;
            return this;
        }

        public Builder withItemsResponseCount(int itemResponseCount) {
            this.itemResponseCount = itemResponseCount;
            return this;
        }

        public ExpandableExam build() {
            return new ExpandableExam(this);
        }
    }

    public Exam getExam() {
        return exam;
    }

    public List<ExamAccommodation> getExamAccommodations() {
        return examAccommodations;
    }

    public int getItemsResponseCount() {
        return itemResponseCount;
    }
}
