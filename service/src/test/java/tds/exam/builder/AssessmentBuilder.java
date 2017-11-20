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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import tds.assessment.Assessment;
import tds.assessment.ItemConstraint;
import tds.assessment.Segment;
import tds.common.Algorithm;

public class AssessmentBuilder {
    public static final String DEFAULT_ASSESSMENT_KEY = "(SBAC_PT)IRP-Perf-ELA-3-Summer-2015-2016";

    private String key = DEFAULT_ASSESSMENT_KEY;
    private String assessmentId = "IRP-Perf-ELA-3";
    private Algorithm selectionAlgorithm = Algorithm.FIXED_FORM;
    private float startAbility = 0;
    private int prefetch = 2;
    private String subject = "ENGLISH";
    private List<Segment> segments;
    private List<ItemConstraint> itemConstraints = new ArrayList<>();
    private boolean initialAbilityBySubject;
    private float abilitySlope;
    private float abilityIntercept;
    private Instant fieldTestStartDate;
    private Instant fieldTestEndDate;
    private String accommodationFamily = "ELA";
    private boolean multiStageBraille = false;
    private boolean forceComplete = false;

    public AssessmentBuilder() {
        segments = Collections.singletonList(new SegmentBuilder().build());
    }

    public Assessment build() {
        Assessment assessment = new Assessment();
        assessment.setKey(key);
        assessment.setAssessmentId(assessmentId);
        assessment.setSegments(segments);
        assessment.setSelectionAlgorithm(selectionAlgorithm);
        assessment.setStartAbility(startAbility);
        assessment.setFieldTestStartDate(fieldTestStartDate);
        assessment.setFieldTestEndDate(fieldTestEndDate);
        assessment.setAbilitySlope(abilitySlope);
        assessment.setAbilityIntercept(abilityIntercept);
        assessment.setInitialAbilityBySubject(initialAbilityBySubject);
        assessment.setSubject(subject);
        assessment.setAccommodationFamily(accommodationFamily);
        assessment.setItemConstraints(itemConstraints);
        assessment.setMultiStageBraille(multiStageBraille);
        assessment.setForceComplete(forceComplete);
        return assessment;
    }

    public AssessmentBuilder withKey(final String key) {
        this.key = key;
        return this;
    }

    public AssessmentBuilder withInitialAbilityBySubject(boolean initialAbilityBySubject) {
        this.initialAbilityBySubject = initialAbilityBySubject;
        return this;
    }

    public AssessmentBuilder withAbilitySlope(float abilitySlope) {
        this.abilitySlope = abilitySlope;
        return this;
    }

    public AssessmentBuilder withAbilityIntercept(final float abilityIntercept) {
        this.abilityIntercept = abilityIntercept;
        return this;
    }

    public AssessmentBuilder withAssessmentId(final String assessmentID) {
        this.assessmentId = assessmentID;
        return this;
    }

    public AssessmentBuilder withSelectionAlgorithm(final Algorithm selectionAlgorithm) {
        this.selectionAlgorithm = selectionAlgorithm;
        return this;
    }

    public AssessmentBuilder withStartAbility(final float ability) {
        this.startAbility = ability;
        return this;
    }

    public AssessmentBuilder withSubject(final String subjectName) {
        this.subject = subjectName;
        return this;
    }

    public AssessmentBuilder withSegments(final List<Segment> segments) {
        this.segments = segments;
        return this;
    }

    public AssessmentBuilder withFieldTestStartDate(final Instant fieldTestStartDate) {
        this.fieldTestStartDate = fieldTestStartDate;
        return this;
    }

    public AssessmentBuilder withFieldTestEndDate(final Instant fieldTestEndDate) {
        this.fieldTestEndDate = fieldTestEndDate;
        return this;
    }

    public AssessmentBuilder withAccommodationFamily(final String accommodationFamily) {
        this.accommodationFamily = accommodationFamily;
        return this;
    }

    public AssessmentBuilder withMultiStageBraille(final boolean multiStageBraille) {
        this.multiStageBraille = multiStageBraille;
        return this;
    }

    public AssessmentBuilder withForceComplete(final boolean forceComplete) {
        this.forceComplete = forceComplete;
        return this;
    }
}
