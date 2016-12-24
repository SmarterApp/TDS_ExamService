package tds.exam.builder;

import org.joda.time.Instant;

import java.util.Collections;
import java.util.List;

import tds.assessment.Algorithm;
import tds.assessment.Assessment;
import tds.assessment.Segment;

public class AssessmentBuilder {
    private String key = "(SBAC_PT)IRP-Perf-ELA-3-Summer-2015-2016";
    private String assessmentId = "IRP-Perf-ELA-3";
    private Algorithm selectionAlgorithm = Algorithm.FIXED_FORM;
    private float startAbility = 0;
    private int prefetch = 2;
    private String subject = "ENGLISH";
    private List<Segment> segments;
    private boolean initialAbilityBySubject;
    private float abilitySlope;
    private float abilityIntercept;
    private Instant fieldTestStartDate;
    private Instant fieldTestEndDate;

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
        return assessment;
    }

    public AssessmentBuilder withKey(String key) {
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

    public AssessmentBuilder withAbilityIntercept(float abilityIntercept) {
        this.abilityIntercept = abilityIntercept;
        return this;
    }

    public AssessmentBuilder withAssessmentId(String assessmentID) {
        this.assessmentId = assessmentID;
        return this;
    }

    public AssessmentBuilder withSelectionAlgorithm(Algorithm selectionAlgorithm) {
        this.selectionAlgorithm = selectionAlgorithm;
        return this;
    }

    public AssessmentBuilder withStartAbility(float ability) {
        this.startAbility = ability;
        return this;
    }

    public AssessmentBuilder withSubject(String subjectName) {
        this.subject = subjectName;
        return this;
    }

    public AssessmentBuilder withSegments(List<Segment> segments) {
        this.segments = segments;
        return this;
    }

    public AssessmentBuilder withFieldTestStartDate(Instant fieldTestStartDate) {
        this.fieldTestStartDate = fieldTestStartDate;
        return this;
    }

    public AssessmentBuilder withFieldTestEndDate(Instant fieldTestEndDate) {
        this.fieldTestEndDate = fieldTestEndDate;
        return this;
    }
}
