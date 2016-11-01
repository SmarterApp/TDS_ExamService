package tds.exam.builder;

import java.util.Collections;
import java.util.List;

import tds.assessment.Assessment;
import tds.assessment.Segment;

public class AssessmentBuilder {
    private String key = "(SBAC_PT)IRP-Perf-ELA-3-Summer-2015-2016";
    private String assessmentId = "IRP-Perf-ELA-3";
    private String selectionAlgorithm = "fixedform";
    private float startAbility = 0;
    private String subjectName = "ENGLISH";
    private List<Segment> segments;

    public AssessmentBuilder() {
        segments = Collections.singletonList(new SegmentBuilder().build());
    }

    public Assessment build() {
        return new Assessment(key, assessmentId, segments, selectionAlgorithm, startAbility);
    }

    public AssessmentBuilder withKey(String key) {
        this.key = key;
        return this;
    }

    public AssessmentBuilder withAssessmentId(String assessmentID) {
        this.assessmentId = assessmentID;
        return this;
    }

    public AssessmentBuilder withSelectionAlgorithm(String selectionAlgorithm) {
        this.selectionAlgorithm = selectionAlgorithm;
        return this;
    }

    public AssessmentBuilder withStartAbility(float ability) {
        this.startAbility = ability;
        return this;
    }

    public AssessmentBuilder withSubjectName(String subjectName) {
        this.subjectName = subjectName;
        return this;
    }

    public AssessmentBuilder withSegments(List<Segment> segments) {
        this.segments = segments;
        return this;
    }
}
