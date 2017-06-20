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

package tds.exam;

import org.joda.time.Instant;

import java.util.UUID;

import static tds.common.util.Preconditions.checkNotNull;

/**
 * Represents a relationship from the student package
 */
public class ExamineeRelationship {
    private long id;
    private UUID examId;
    private String name;
    private String value;
    private String type;
    private ExamineeContext context;
    private Instant createdAt;

    /**
     * Private constructor for frameworks
     */
    private ExamineeRelationship() {
    }

    public ExamineeRelationship(Builder builder) {
        this.id = builder.id;
        this.examId = builder.examId;
        this.name = builder.name;
        this.value = builder.value;
        this.type = builder.type;
        this.context = builder.context;
        this.createdAt = builder.createdAt;
    }

    public static class Builder {
        private long id;
        private UUID examId;
        private String name;
        private String value;
        private String type;
        private ExamineeContext context;
        private Instant createdAt;

        public ExamineeRelationship build() {
            return new ExamineeRelationship(this);
        }

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withExamId(UUID examId) {
            this.examId = examId;
            return this;
        }

        public Builder withName(String name) {
            this.name = checkNotNull(name);
            return this;
        }

        public Builder withValue(String value) {
            this.value = checkNotNull(value);
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withContext(ExamineeContext context) {
            this.context = context;
            return this;
        }

        public Builder withCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }
    }

    /**
     * @return The unique identifier of this {@link tds.exam.ExamineeRelationship}
     */
    public long getId() {
        return id;
    }

    /**
     * @return The identifier of the {@link tds.exam.Exam} to which this {@link tds.exam.ExamineeRelationship} belongs
     */
    public UUID getExamId() {
        return examId;
    }

    /**
     * @return The name (referred to as "tds_id" in the legacy student application) of this
     * {@link tds.exam.ExamineeRelationship}
     */
    public String getName() {
        return name;
    }

    /**
     * @return The value for this {@link tds.exam.ExamineeRelationship}
     */
    public String getValue() {
        return value;
    }

    /**
     * @return The relationship type for this {@link tds.exam.ExamineeRelationship}
     */
    public String getType() {
        return type;
    }

    /**
     * @return The {@link tds.exam.ExamineeContext} describing when this {@link tds.exam.ExamineeRelationship} was
     * captured
     */
    public ExamineeContext getContext() {
        return context;
    }

    /**
     * @return The date and time when this {@link tds.exam.ExamineeRelationship} was created
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExamineeRelationship)) return false;

        ExamineeRelationship that = (ExamineeRelationship) o;

        if (getId() != that.getId()) return false;
        if (!getExamId().equals(that.getExamId())) return false;
        if (!getName().equals(that.getName())) return false;
        if (!getValue().equals(that.getValue())) return false;
        if (!getType().equals(that.getType())) return false;
        if (getContext() != that.getContext()) return false;
        return getCreatedAt().equals(that.getCreatedAt());
    }

    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));
        result = 31 * result + getExamId().hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + getValue().hashCode();
        result = 31 * result + getType().hashCode();
        result = 31 * result + getContext().hashCode();
        result = 31 * result + getCreatedAt().hashCode();
        return result;
    }
}
