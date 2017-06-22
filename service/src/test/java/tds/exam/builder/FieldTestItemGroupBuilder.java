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


import java.time.Instant;
import java.util.UUID;

import tds.exam.models.FieldTestItemGroup;

public class FieldTestItemGroupBuilder {
    private String groupKey = "group-key";
    private String groupId = "group-id";
    private String blockId = "A";
    private String languageCode = "ENU";
    private UUID examId = UUID.randomUUID();
    private int itemCount = 1;
    private String segmentKey = "segment-key";
    private Instant deletedAt;
    private Integer positionAdministered = null;
    private Instant administeredAt;

    public FieldTestItemGroupBuilder(String groupKey) {
        this.groupKey = groupKey;
    }

    public FieldTestItemGroup build() {
        FieldTestItemGroup.Builder builder = new FieldTestItemGroup.Builder()
            .withGroupKey(groupKey)
            .withGroupId(groupId)
            .withBlockId(blockId)
            .withExamId(examId)
            .withLanguageCode(languageCode)
            .withItemCount(itemCount)
            .withSegmentKey(segmentKey)
            .withAdministeredAt(administeredAt)
            .withDeletedAt(deletedAt);

        if(positionAdministered != null) {
            builder.withPosition(positionAdministered);
        }

        return builder.build();
    }

    public FieldTestItemGroupBuilder withExamId(UUID examId) {
        this.examId = examId;
        return this;
    }

    public FieldTestItemGroupBuilder withGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public FieldTestItemGroupBuilder withItemCount(int itemCount) {
        this.itemCount = itemCount;
        return this;
    }

    public FieldTestItemGroupBuilder withSegmentKey(String segmentKey) {
        this.segmentKey = segmentKey;
        return this;
    }

    public FieldTestItemGroupBuilder withDeletedAt(final Instant deletedAt) {
        this.deletedAt = deletedAt;
        return this;
    }

    public FieldTestItemGroupBuilder withPositionAdministered(final Integer positionAdministered) {
        this.positionAdministered = positionAdministered;
        return this;
    }

    public FieldTestItemGroupBuilder withAdministeredAt(final Instant administeredAt) {
        this.administeredAt = administeredAt;
        return this;
    }
}
