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

package tds.exam.builder;

import org.joda.time.Instant;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import tds.common.Algorithm;
import tds.exam.ExamSegment;

/**
 * Build an {@link tds.exam.ExamSegment} populated with test data.
 */
public class ExamSegmentBuilder {
    private UUID examId = UUID.randomUUID();
    private String segmentKey = "segment-key-1";
    private String segmentId = "segment-id-1";
    private int segmentPosition = 1;
    private String formKey = "segment-form-key-1";
    private String formId = "segment-form-id-1";
    private Algorithm algorithm = Algorithm.FIXED_FORM;
    private int examItemCount = 1;
    private int fieldTestItemCount = 0;
    private boolean isPermeable = false;
    private String restorePermeableCondition = "restore-permeable-condition";
    private String formCohort = "form-cohort-1";
    private boolean isSatisfied = true;
    private Instant exitedAt;
    private Set<String> itemPool = new HashSet<>(Arrays.asList("187-1234", "187-5678"));
    private int poolCount = 2;
    private Instant createdAt;

    public ExamSegment build() {
        return new ExamSegment.Builder()
            .withExamId(examId)
            .withSegmentKey(segmentKey)
            .withSegmentId(segmentId)
            .withSegmentPosition(segmentPosition)
            .withFormKey(formKey)
            .withFormId(formId)
            .withAlgorithm(algorithm)
            .withExamItemCount(examItemCount)
            .withFieldTestItemCount(fieldTestItemCount)
            .withPermeable(isPermeable)
            .withRestorePermeableCondition(restorePermeableCondition)
            .withFormCohort(formCohort)
            .withSatisfied(isSatisfied)
            .withExitedAt(exitedAt)
            .withItemPool(itemPool)
            .withPoolCount(poolCount)
            .withCreatedAt(createdAt)
            .build();
    }

    public ExamSegmentBuilder withExamId(UUID examId) {
        this.examId = examId;
        return this;
    }

    public ExamSegmentBuilder withSegmentKey(String segmentKey) {
        this.segmentKey = segmentKey;
        return this;
    }

    public ExamSegmentBuilder withSegmentId(String segmentId) {
        this.segmentId = segmentId;
        return this;
    }

    public ExamSegmentBuilder withSegmentPosition(int segmentPosition) {
        this.segmentPosition = segmentPosition;
        return this;
    }

    public ExamSegmentBuilder withFormKey(String formKey) {
        this.formKey = formKey;
        return this;
    }

    public ExamSegmentBuilder withFormId(String formId) {
        this.formId = formId;
        return this;
    }

    public ExamSegmentBuilder withAlgorithm(Algorithm algorithm) {
        this.algorithm = algorithm;
        return this;
    }

    public ExamSegmentBuilder withExamItemCount(int examItemCount) {
        this.examItemCount = examItemCount;
        return this;
    }

    public ExamSegmentBuilder withFieldTestItemCount(int fieldTestItemCount) {
        this.fieldTestItemCount = fieldTestItemCount;
        return this;
    }

    public ExamSegmentBuilder withIsPermeable(boolean isPermeable) {
        this.isPermeable = isPermeable;
        return this;
    }

    public ExamSegmentBuilder withRestorePermeableCondition(String restorePermeableCondition) {
        this.restorePermeableCondition = restorePermeableCondition;
        return this;
    }

    public ExamSegmentBuilder withFormCohort(String formCohort) {
        this.formCohort = formCohort;
        return this;
    }

    public ExamSegmentBuilder withIsSatisfied(boolean isSatisfied) {
        this.isSatisfied = isSatisfied;
        return this;
    }

    public ExamSegmentBuilder withExitedAt(Instant exitedAt) {
        this.exitedAt = exitedAt;
        return this;
    }

    public ExamSegmentBuilder withItemPool(Set<String> itemPool) {
        this.itemPool = itemPool;
        return this;
    }

    public ExamSegmentBuilder withPoolCount(int poolCount) {
        this.poolCount = poolCount;
        return this;
    }

    public ExamSegmentBuilder withCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
