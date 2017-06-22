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

import java.util.UUID;

import tds.exam.ExamineeContext;
import tds.exam.ExamineeRelationship;

/**
 * Build an {@link tds.exam.ExamineeRelationship} with sample data
 */
public class ExamineeRelationshipBuilder {
    private long id = 0L;
    private UUID examId = UUID.randomUUID();
    private String name = "UnitTestRelationshipName";
    private String value = "UnitTestRelationshipValue";
    private String type = "UnitTestRelationshipType";
    private ExamineeContext context = ExamineeContext.INITIAL;
    private Instant createdAt = Instant.now();

    public ExamineeRelationship build() {
        return new ExamineeRelationship.Builder()
            .withId(id)
            .withExamId(examId)
            .withContext(context)
            .withName(name)
            .withValue(value)
            .withType(type)
            .withCreatedAt(createdAt)
            .build();
    }

    public ExamineeRelationshipBuilder withId(long id) {
        this.id = id;
        return this;
    }

    public ExamineeRelationshipBuilder withExamId(UUID examId) {
        this.examId = examId;
        return this;
    }

    public ExamineeRelationshipBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ExamineeRelationshipBuilder withValue(String value) {
        this.value = value;
        return this;
    }

    public ExamineeRelationshipBuilder withType(String type) {
        this.type = type;
        return this;
    }

    public ExamineeRelationshipBuilder withContext(ExamineeContext context) {
        this.context = context;
        return this;
    }

    public ExamineeRelationshipBuilder withCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
