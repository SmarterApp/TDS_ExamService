package tds.score.model;

import com.google.auto.value.AutoValue;

import java.util.UUID;

@AutoValue
public abstract class ExamInstance {
    public abstract UUID getExamId();
    public abstract UUID getSessionId();
    public abstract UUID getBrowserId();
    public abstract String getClientName();
}
