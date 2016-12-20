package tds.exam.builder;

import java.util.ArrayList;
import java.util.List;

import tds.assessment.Algorithm;
import tds.assessment.Form;
import tds.assessment.Segment;

public class SegmentBuilder {
    private String key = "(SBAC_PT)IRP-Perf-ELA-3-Summer-2015-2016";
    private String segmentId = "IRP-Perf-ELA-3";
    private Algorithm selectionAlgorithm = Algorithm.FIXED_FORM;
    private float startAbility = 0;
    private String subjectName = "ENGLISH";
    private String assessmentKey = "(SBAC_PT)IRP-Perf-ELA-3-Summer-2015-2016";
    private int maxItems = 10;
    private int position;
    private List<Form> forms = new ArrayList<>();

    public Segment build() {
        Segment segment = new Segment(key, selectionAlgorithm);
        segment.setSegmentId(segmentId);
        segment.setStartAbility(startAbility);
        segment.setSubject(subjectName);
        segment.setAssessmentKey(assessmentKey);
        segment.setMaxItems(maxItems);
        segment.setPosition(position);
        segment.setForms(forms);
        return segment;
    }

    public SegmentBuilder withKey(String key) {
        this.key = key;
        return this;
    }

    public SegmentBuilder withSegmentId(String segmentId) {
        this.segmentId = segmentId;
        return this;
    }

    public SegmentBuilder withSelectionAlgorithm(Algorithm selectionAlgorithm) {
        this.selectionAlgorithm = selectionAlgorithm;
        return this;
    }

    public SegmentBuilder withStartAbility(float ability) {
        this.startAbility = ability;
        return this;
    }

    public SegmentBuilder withSubjectName(String subjectName) {
        this.subjectName = subjectName;
        return this;
    }

    public SegmentBuilder withAssessmentKey(String assessmentKey) {
        this.assessmentKey = assessmentKey;
        return this;
    }

    public SegmentBuilder withMaxItems(int maxItems) {
        this.maxItems = maxItems;
        return this;
    }

    public SegmentBuilder withPosition(int position) {
        this.position = position;
        return this;
    }

    public SegmentBuilder withForms(List<Form> forms) {
        this.forms = forms;
        return this;
    }
}
