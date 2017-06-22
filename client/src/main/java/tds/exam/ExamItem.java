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

import com.google.common.base.Optional;
import org.joda.time.Instant;

import java.util.UUID;

import static tds.common.util.Preconditions.checkNotNull;

/**
 * Represent the item on a page of an exam
 */
public class ExamItem {
    private UUID id;
    private UUID examPageId;
    private String itemKey;
    private String groupId;
    private long assessmentItemBankKey;
    private long assessmentItemKey;
    private String itemType;
    private int position;
    private boolean required;
    private boolean fieldTest;
    private String itemFilePath;
    private String stimulusFilePath;
    private ExamItemResponse response;
    private Instant createdAt;

    /**
     * Private constructor for frameworks
     */
    private ExamItem() {
    }


    private ExamItem(Builder builder) {
        id = checkNotNull(builder.id);
        examPageId = checkNotNull(builder.examPageId);
        itemKey = checkNotNull(builder.itemKey);
        assessmentItemBankKey = checkNotNull(builder.assessmentItemBankKey);
        assessmentItemKey = builder.assessmentItemKey;
        itemType = checkNotNull(builder.itemType);
        position = builder.position;
        required = builder.required;
        fieldTest = builder.fieldTest;
        itemFilePath = checkNotNull(builder.itemFilePath);
        stimulusFilePath = builder.stimulusFilePath;
        response = builder.response;
        createdAt = builder.createdAt;
        groupId = checkNotNull(builder.groupId);
    }

    public static final class Builder {
        private UUID id;
        private UUID examPageId;
        private String itemKey;
        private long assessmentItemBankKey;
        private long assessmentItemKey;
        private String itemType;
        private int position;
        private boolean required;
        private boolean fieldTest;
        private String itemFilePath;
        private String stimulusFilePath;
        private ExamItemResponse response;
        private Instant createdAt;
        private String groupId;

        public Builder(final UUID id) {
            this.id = checkNotNull(id);
        }

        public Builder withId(final UUID id) {
            this.id = id;
            return this;
        }

        public Builder withExamPageId(final UUID examPageId) {
            this.examPageId = checkNotNull(examPageId);
            return this;
        }

        public Builder withItemKey(final String itemKey) {
            this.itemKey = checkNotNull(itemKey);
            return this;
        }

        public Builder withAssessmentItemBankKey(final long assessmentItemBankKey) {
            this.assessmentItemBankKey = checkNotNull(assessmentItemBankKey);
            return this;
        }

        public Builder withAssessmentItemKey(final long assessmentItemKey) {
            this.assessmentItemKey = assessmentItemKey;
            return this;
        }

        public Builder withItemType(final String itemType) {
            this.itemType = checkNotNull(itemType);
            return this;
        }

        public Builder withPosition(final int position) {
            if (position < 1) {
                throw new IllegalArgumentException("Item position must be greater than 0");
            }

            this.position = position;
            return this;
        }

        public Builder withRequired(final boolean required) {
            this.required = required;
            return this;
        }

        public Builder withFieldTest(final boolean fieldTest) {
            this.fieldTest = fieldTest;
            return this;
        }

        public Builder withResponse(final ExamItemResponse response) {
            this.response = response;
            return this;
        }

        public Builder withItemFilePath(final String itemFilePath) {
            this.itemFilePath = checkNotNull(itemFilePath, "Item file path cannot be null");
            return this;
        }

        public Builder withStimulusFilePath(final String stimulusFilePath) {
            this.stimulusFilePath = stimulusFilePath;
            return this;
        }

        public Builder withCreatedAt(final Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withGroupId(final String groupId){
            this.groupId = checkNotNull(groupId);
            return this;
        }

        public static Builder fromExamItem(final ExamItem examItem) {
            return new Builder(examItem.getId())
                .withId(examItem.getId())
                .withExamPageId(examItem.getExamPageId())
                .withItemKey(examItem.getItemKey())
                .withAssessmentItemBankKey(examItem.getAssessmentItemBankKey())
                .withAssessmentItemKey(examItem.getAssessmentItemKey())
                .withItemType(examItem.getItemType())
                .withPosition(examItem.getPosition())
                .withRequired(examItem.isRequired())
                .withFieldTest(examItem.isFieldTest())
                .withResponse(examItem.getResponse().orNull())
                .withItemFilePath(examItem.getItemFilePath())
                .withStimulusFilePath(examItem.getStimulusFilePath().orNull())
                .withCreatedAt(examItem.getCreatedAt())
                .withGroupId(examItem.getGroupId());
        }

        public ExamItem build() {
            return new ExamItem(this);
        }
    }

    /**
     * @return The id of this {@link tds.exam.ExamItem}
     */
    public UUID getId() {
        return id;
    }

    /**
     * @return The id of the {@link tds.exam.ExamPage} that owns this {@link tds.exam.ExamItem}
     */
    public UUID getExamPageId() {
        return examPageId;
    }

    /**
     * @return The item key for this {@link tds.exam.ExamItem}
     */
    public String getItemKey() {
        return itemKey;
    }

    /**
     * @return The bank key of this {@link tds.exam.ExamItem} as represented in the itembank database
     */
    public long getAssessmentItemBankKey() {
        return assessmentItemBankKey;
    }

    /**
     * @return The item key of this {@link tds.exam.ExamItem} represented in the itembank database
     */
    public long getAssessmentItemKey() {
        return assessmentItemKey;
    }

    /**
     * @return The code representing the type of this {@link tds.exam.ExamItem} (e.g. "ER" for Extended Response, "MS"
     * for Multi-Select)
     */
    public String getItemType() {
        return itemType;
    }

    /**
     * @return The position of this {@link tds.exam.ExamItem} in the exam
     */
    public int getPosition() {
        return position;
    }

    /**
     * @return True if this {@link tds.exam.ExamItem} is marked as required; otherwise false
     */
    public boolean isRequired() {
        return required;
    }

    /**
     * @return Flag indicating whether this is a field test {@link tds.exam.ExamItem}
     */
    public boolean isFieldTest() {
        return fieldTest;
    }

    /**
     * @return The path to this {@link tds.exam.ExamItem}'s XML file
     */
    public String getItemFilePath() {
        return itemFilePath;
    }

    /**
     * @return The path to this {@link tds.exam.ExamItem}'s stimulus file, if one is present
     */
    public Optional<String> getStimulusFilePath() {
        return Optional.fromNullable(stimulusFilePath);
    }

    /**
     * @return The most recent {@link tds.exam.ExamItemResponse} for this {@link tds.exam.ExamItem}, if there is one
     */
    public Optional<ExamItemResponse> getResponse() {
        return Optional.fromNullable(response);
    }

    /**
     * @return The date/time when this {@link tds.exam.ExamItem} was created
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * @return the associated group id
     */
    public String getGroupId() {
        return groupId;
    }
}
