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
    private String subject = "ENGLISH";
    private List<Segment> segments;

    public AssessmentBuilder() {
        segments = Collections.singletonList(new SegmentBuilder().build());
    }

    public Assessment build() {
        return new Assessment.Builder()
            .withKey(key)
            .withAssessmentId(assessmentId)
            .withSegments(segments)
            .withSelectionAlgorithm(selectionAlgorithm)
            .withStartAbility(startAbility)
            .build();
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

    public AssessmentBuilder withSubject(String subjectName) {
        this.subject = subjectName;
        return this;
    }

    public AssessmentBuilder withSegments(List<Segment> segments) {
        this.segments = segments;
        return this;
    }
}
