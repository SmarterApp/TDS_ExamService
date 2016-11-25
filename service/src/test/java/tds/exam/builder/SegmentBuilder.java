package tds.exam.builder;

import java.util.List;

import tds.assessment.Algorithm;
import tds.assessment.ItemProperty;
import tds.assessment.Segment;

import static java.util.Collections.singletonList;

public class SegmentBuilder {
    private String key = "(SBAC_PT)IRP-Perf-ELA-3-Summer-2015-2016";
    private String segmentId = "IRP-Perf-ELA-3";
    private Algorithm selectionAlgorithm = Algorithm.FIXED_FORM;
    private float startAbility = 0;
    private String subjectName = "ENGLISH";
    private String assessmentKey = "(SBAC_PT)IRP-Perf-ELA-3-Summer-2015-2016";
    private List<ItemProperty> languages = singletonList(new ItemProperty("Language", "ENU", "language for assessment"));

    public Segment build() {
        Segment segment = new Segment(key);
        segment.setLanguages(languages);
        segment.setSegmentId(segmentId);
        segment.setSelectionAlgorithm(selectionAlgorithm);
        segment.setStartAbility(startAbility);
        segment.setSubject(subjectName);
        segment.setAssessmentKey(assessmentKey);
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

    public SegmentBuilder withLanguages(List<ItemProperty> languages) {
        this.languages = languages;
        return this;
    }
}
