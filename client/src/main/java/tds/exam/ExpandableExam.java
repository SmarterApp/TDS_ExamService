/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

package tds.exam;

import org.joda.time.Instant;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import tds.exam.wrapper.ExamSegmentWrapper;

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
    private List<ExamineeNote> examineeNotes;
    private List<ExamineeAttribute> examineeAttributes;
    private List<ExamineeRelationship> examineeRelationships;
    private List<ExamSegmentWrapper> examSegmentWrappers;
    private boolean multiStageBraille;
    private Instant forceCompletedAt;
    private int windowAttempts;
    private Map<UUID, Integer> itemResponseUpdates;

    /* Empty private constructor for frameworks */
    private ExpandableExam() {}

    public ExpandableExam(Builder builder) {
        this.exam = builder.exam;
        this.examAccommodations = builder.examAccommodations;
        this.examSegments = builder.examSegments;
        this.examPages = builder.examPages;
        this.examItems = builder.examItems;
        this.examineeNotes = builder.examineeNotes;
        this.examineeAttributes = builder.examineeAttributes;
        this.examineeRelationships = builder.examineeRelationships;
        this.itemsResponseCount = builder.itemsResponseCount;
        this.requestCount = builder.requestCount;
        this.multiStageBraille = builder.multiStageBraille;
        this.forceCompletedAt = builder.forceCompletedAt;
        this.windowAttempts = builder.windowAttempts;
        this.itemResponseUpdates = builder.itemResponseUpdates;
        this.examSegmentWrappers = builder.examSegmentWrappers;
    }

    public static class Builder {
        private Exam exam;
        private List<ExamAccommodation> examAccommodations;
        private List<ExamSegment> examSegments;
        private List<ExamPage> examPages;
        private List<ExamItem> examItems;
        private List<ExamineeNote> examineeNotes;
        private List<ExamineeAttribute> examineeAttributes;
        private List<ExamineeRelationship> examineeRelationships;
        private int itemsResponseCount;
        private int requestCount;
        private boolean multiStageBraille;
        private Instant forceCompletedAt;
        private int windowAttempts;
        private Map<UUID, Integer> itemResponseUpdates;
        private List<ExamSegmentWrapper> examSegmentWrappers;

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

        public Builder withForceCompletedAt(Instant forceCompletedAt) {
            this.forceCompletedAt = forceCompletedAt;
            return this;
        }

        public Builder withWindowAttempts(int windowAttempts) {
            this.windowAttempts = windowAttempts;
            return this;
        }

        public Builder withItemResponseUpdates(Map<UUID, Integer> itemResponseUpdates) {
            this.itemResponseUpdates = itemResponseUpdates;
            return this;
        }

        public Builder withExamSegmentWrappers(List<ExamSegmentWrapper> examSegmentWrappers) {
            this.examSegmentWrappers = examSegmentWrappers;
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
        return (examAccommodations != null) ? examAccommodations : new ArrayList<ExamAccommodation>();
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
     * @return The {@link org.joda.time.Instant} the exam was force-completed at
     */
    public Instant getForceCompletedAt() {
        return forceCompletedAt;
    }

    /**
     * @return The number of exam attempts the student has within a specific assessment and assessment window period
     */
    public int getWindowAttempts() {
        return windowAttempts;
    }

    /**
     * @return The number of updates to each {@link tds.exam.ExamItem} in the exam
     */
    public Map<UUID, Integer> getItemResponseUpdates() {
        return itemResponseUpdates;
    }

    /**
     * @return The {@link tds.exam.wrapper.ExamSegmentWrapper} containing pages, items, and responses
     */
    public List<ExamSegmentWrapper> getExamSegmentWrappers() {
        return examSegmentWrappers;
    }
}
