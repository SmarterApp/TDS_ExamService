package tds.exam;

import org.joda.time.Instant;

import java.util.ArrayList;
import java.util.List;

/**
 * A model representing an {@link tds.exam.Exam} as well as other optional exam-specific data
 */
public class ExpandableExam {
    private Exam exam;
    private int itemsResponseCount;
    private int requestCount;
    private List<ExamAccommodation> examAccommodations;
    private List<ExamSegment> examSegments;
    private List<ExamPage> examPages;
    private List<ExamItem> examItems;
    private List<ExamItemResponse> examItemResponses;
    private List<ExamineeNote> examineeNotes;
    private List<ExamineeAttribute> examineeAttributes;
    private List<ExamineeRelationship> examineeRelationships;
    private boolean multiStageBraille;
    private Instant forceCompletedAt;

    /* Empty private constructor for frameworks */
    private ExpandableExam() {}

    public ExpandableExam(Builder builder) {
        this.exam = builder.exam;
        this.examAccommodations = builder.examAccommodations;
        this.examSegments = builder.examSegments;
        this.examPages = builder.examPages;
        this.examItems = builder.examItems;
        this.examItemResponses = builder.examItemResponses;
        this.examineeNotes = builder.examineeNotes;
        this.examineeAttributes = builder.examineeAttributes;
        this.examineeRelationships = builder.examineeRelationships;
        this.itemsResponseCount = builder.itemsResponseCount;
        this.requestCount = builder.requestCount;
        this.multiStageBraille = builder.multiStageBraille;
        this.forceCompletedAt = builder.forceCompletedAt;
    }

    public static class Builder {
        private Exam exam;
        private List<ExamAccommodation> examAccommodations;
        private List<ExamSegment> examSegments;
        private List<ExamPage> examPages;
        private List<ExamItem> examItems;
        private List<ExamItemResponse> examItemResponses;
        private List<ExamineeNote> examineeNotes;
        private List<ExamineeAttribute> examineeAttributes;
        private List<ExamineeRelationship> examineeRelationships;
        private int itemsResponseCount;
        private int requestCount;
        private boolean multiStageBraille;
        private Instant startedAt;
        private Instant completedAt;
        private Instant forceCompletedAt;

        public Builder(Exam exam) {
            this.exam = exam;
        }

        public Builder withExamAccommodations(List<ExamAccommodation> examAccommodations) {
            this.examAccommodations = examAccommodations;
            return this;
        }

        public Builder withExamSegments(List<ExamSegment> examSegments) {
            this.examSegments = examSegments;
            return this;
        }

        public Builder withExamPages(List<ExamPage> examPages) {
            this.examPages = examPages;
            return this;
        }

        public Builder withExamItems(List<ExamItem> examItems) {
            this.examItems = examItems;
            return this;
        }

        public Builder withExamItemResponses(List<ExamItemResponse> examItemResponses) {
            this.examItemResponses = examItemResponses;
            return this;
        }

        public Builder withExamineeNotes(List<ExamineeNote> examineeNotes) {
            this.examineeNotes = examineeNotes;
            return this;
        }

        public Builder withExamineeAttributes(List<ExamineeAttribute> examineeAttributes) {
            this.examineeAttributes = examineeAttributes;
            return this;
        }

        public Builder withExamineeRelationship(List<ExamineeRelationship> examineeRelationships) {
            this.examineeRelationships = examineeRelationships;
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

        public Builder withStartedAt(Instant startedAt) {
            this.startedAt = startedAt;
            return this;
        }

        public Builder withCompletedAt(Instant completedAt) {
            this.completedAt = completedAt;
            return this;
        }

        public Builder withForceCompletedAt(Instant forceCompletedAt) {
            this.forceCompletedAt = forceCompletedAt;
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

    /**
     * @return The exam segments for the given examId
     */
    public List<ExamSegment> getExamSegments() {
        return (examSegments != null) ? examSegments : new ArrayList<ExamSegment>();
    }

    /**
     * @return The exam pages for the given examId
     */
    public List<ExamPage> getExamPages() {
        return (examPages != null) ? examPages : new ArrayList<ExamPage>();
    }

    /**
     * @return The exam items for the given examId
     */
    public List<ExamItem> getExamItems() {
        return (examItems != null) ? examItems : new ArrayList<ExamItem>();
    }

    /**
     * @return The exam item responses for the given examId
     */
    public List<ExamItemResponse> getExamItemResponses() {
        return (examItemResponses != null) ? examItemResponses : new ArrayList<ExamItemResponse>();
    }

    /**
     * @return The examinee notes for the given examId
     */
    public List<ExamineeNote> getExamineeNotes() {
        return (examineeNotes != null) ? examineeNotes : new ArrayList<ExamineeNote>();
    }

    /**
     * @return The examinee attributes
     */
    public List<ExamineeAttribute> getExamineeAttributes() {
        return (examineeAttributes != null) ? examineeAttributes : new ArrayList<ExamineeAttribute>();
    }

    /**
     * @return The exam relationships for the given examId
     */
    public List<ExamineeRelationship> getExamineeRelationships() {
        return (examineeRelationships != null) ? examineeRelationships : new ArrayList<ExamineeRelationship>();
    }

    /**
     * @return the {@link org.joda.time.Instant} the exam was force-completed at
     */
    public Instant getForceCompletedAt() {
        return forceCompletedAt;
    }
}
