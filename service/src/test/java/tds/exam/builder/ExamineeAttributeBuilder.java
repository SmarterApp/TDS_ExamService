package tds.exam.builder;

import org.joda.time.Instant;

import java.util.UUID;

import tds.exam.ExamineeAttribute;
import tds.exam.ExamineeContext;

/**
 * Build an {@link tds.exam.ExamineeAttribute} with sample data
 */
public class ExamineeAttributeBuilder {
    private long id = 0L;
    private UUID examId = UUID.randomUUID();
    private ExamineeContext context = ExamineeContext.INITIAL;
    private String name = "UnitTestName";
    private String value = "UnitTestValue";
    private Instant createdAt = Instant.now();

    public ExamineeAttribute build() {
        return new ExamineeAttribute.Builder()
            .withId(id)
            .withExamId(examId)
            .withContext(context)
            .withName(name)
            .withValue(value)
            .withCreatedAt(createdAt)
            .build();
    }

    public ExamineeAttributeBuilder withId(long id) {
        this.id = id;
        return this;
    }

    public ExamineeAttributeBuilder withExamId(UUID examId) {
        this.examId = examId;
        return this;
    }

    public ExamineeAttributeBuilder withContext(ExamineeContext context) {
        this.context = context;
        return this;
    }

    public ExamineeAttributeBuilder withName(String name) {
        this.name = name;
        return this;
    }

    public ExamineeAttributeBuilder withValue(String value) {
        this.value = value;
        return this;
    }

    public ExamineeAttributeBuilder withCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
        return this;
    }
}
