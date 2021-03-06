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

import java.util.ArrayList;
import java.util.List;

import tds.assessment.Form;
import tds.assessment.Item;
import tds.assessment.Segment;
import tds.common.Algorithm;

public class SegmentBuilder {
    public static final String DEFAULT_SEGMENT_KEY = "(SBAC_PT)IRP-Perf-ELA-3-Summer-2015-2016";

    private String key = DEFAULT_SEGMENT_KEY;
    private String segmentId = "IRP-Perf-ELA-3";
    private Algorithm selectionAlgorithm = Algorithm.FIXED_FORM;
    private float startAbility = 0;
    private String subjectName = "ENGLISH";
    private String assessmentKey = "(SBAC_PT)IRP-Perf-ELA-3-Summer-2015-2016";
    private int maxItems = 10;
    private int position;
    private List<Form> forms = new ArrayList<>();
    private List<Item> items = new ArrayList<>();
    private Integer fieldTestStartPosition = 3;
    private Integer fieldTestEndPosition = 10;
    private int fieldTestMaxItems = 4;
    private int fieldTestMinItems = 2;

    public Segment build() {
        Segment segment = new Segment(key, selectionAlgorithm);
        segment.setSegmentId(segmentId);
        segment.setStartAbility(startAbility);
        segment.setSubject(subjectName);
        segment.setAssessmentKey(assessmentKey);
        segment.setMaxItems(maxItems);
        segment.setPosition(position);
        segment.setForms(forms);
        segment.setItems(items);
        segment.setFieldTestMaxItems(fieldTestMaxItems);
        segment.setFieldTestMinItems(fieldTestMinItems);
        segment.setFieldTestEndPosition(fieldTestEndPosition);
        segment.setFieldTestStartPosition(fieldTestStartPosition);
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

    public SegmentBuilder withItems(List<Item> items) {
        this.items = items;
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

    public SegmentBuilder withFieldTestStartPosition(Integer startPosition) {
        this.fieldTestStartPosition = startPosition;
        return this;
    }

    public SegmentBuilder withFieldTestEndPosition(Integer endPosition) {
        this.fieldTestEndPosition = endPosition;
        return this;
    }

    public SegmentBuilder withFieldTestMaxItems(int maxItems) {
        this.fieldTestMaxItems = maxItems;
        return this;
    }

    public SegmentBuilder withFieldTestMinItems(int minItems) {
        this.fieldTestMinItems = minItems;
        return this;
    }
}
