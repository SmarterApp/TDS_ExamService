package tds.score.model;

import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class ExamInstance {
    public abstract UUID getExamId();
    public abstract UUID getSessionId();
    public abstract UUID getBrowserId();
    public abstract String getClientName();

    public static ExamInstance create(final UUID newExamId, final UUID newSessionId, final UUID newBrowserId, final String newClientName) {
        return new AutoValue_ExamInstance(newExamId, newSessionId, newBrowserId, newClientName);
    }
}
