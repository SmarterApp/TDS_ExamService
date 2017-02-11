package tds.exam;

import org.joda.time.Instant;

import java.util.UUID;

/**
 * Represents an attribute from the student package
 */
public class ExamineeAttribute {
    private long id;
    private UUID examId;
    private ExamineeContext context;
    private String name;
    private String value;
    private Instant createdAt;

    /**
     * Private constructor for frameworks
     */
    private ExamineeAttribute() {
    }

    public ExamineeAttribute(Builder builder) {
        this.id = builder.id;
        this.examId = builder.examId;
        this.context = builder.context;
        this.name = builder.name;
        this.value = builder.value;
        this.createdAt = builder.createdAt;
    }

    public static class Builder {
        private long id;
        private UUID examId;
        private ExamineeContext context;
        private String name;
        private String value;
        private Instant createdAt;

        public Builder withId(long id) {
            this.id = id;
            return this;
        }

        public Builder withExamId(UUID examId) {
            this.examId = examId;
            return this;
        }

        public Builder withContext(ExamineeContext context) {
            this.context = context;
            return this;
        }

        public Builder withName(String name) {
            this.name = name;
            return this;
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Builder withCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public ExamineeAttribute build() {
            return new ExamineeAttribute(this);
        }
    }

    /**
     * @return The unique identifier of this {@link tds.exam.ExamineeAttribute}
     */
    public long getId() {
        return id;
    }

    /**
     * @return The identifier of the {@link tds.exam.Exam} to which this {@link tds.exam.ExamineeAttribute} is
     * associated
     */
    public UUID getExamId() {
        return examId;
    }

    /**
     * @return The {@link tds.exam.ExamineeContext} describing when this {@link tds.exam.ExamineeAttribute} was captured
     */
    public ExamineeContext getContext() {
        return context;
    }

    /**
     * @return the name (referred to as "tds_id" in the legacy student application) of this
     * {@link tds.exam.ExamineeAttribute}
     */
    public String getName() {
        return name;
    }

    /**
     * @return The value of this {@link tds.exam.ExamineeAttribute}
     */
    public String getValue() {
        return value;
    }

    /**
     * @return The date and time when this {@link tds.exam.ExamineeAttribute} was created
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ExamineeAttribute)) return false;

        ExamineeAttribute that = (ExamineeAttribute) o;

        if (getId() != that.getId()) return false;
        if (!getExamId().equals(that.getExamId())) return false;
        if (getContext() != that.getContext()) return false;
        if (!getName().equals(that.getName())) return false;
        if (!getValue().equals(that.getValue())) return false;
        return getCreatedAt().equals(that.getCreatedAt());
    }

    @Override
    public int hashCode() {
        int result = (int) (getId() ^ (getId() >>> 32));
        result = 31 * result + getExamId().hashCode();
        result = 31 * result + getContext().hashCode();
        result = 31 * result + getName().hashCode();
        result = 31 * result + getValue().hashCode();
        result = 31 * result + getCreatedAt().hashCode();
        return result;
    }
}
