package tds.exam;

import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;

public class ExamStatusCode {
    public static final String STATUS_PAUSED = "paused";
    public static final String STATUS_PENDING = "pending";
    public static final String STATUS_APPROVED = "approved";
    public static final String STATUS_SUSPENDED = "suspended";
    public static final String STATUS_REVIEW = "review";
    public static final String STATUS_INITIALIZING = "initializing";
    public static final String STATUS_STARTED = "started";
    public static final String STATUS_FAILED = "failed";
    public static final String STATUS_DENIED = "denied";
    public static final String STATUS_COMPLETED = "completed";
    public static final String STATUS_SCORED = "scored";
    public static final String STATUS_SEGMENT_ENTRY = "segmentEntry";
    public static final String STATUS_SEGMENT_EXIT = "segmentExit";
    public static final String STATUS_FORCE_COMPLETED = "forceCompleted";
    public static final String STATUS_INVALIDATED = "invalidated";
    public static final String STATUS_EXPIRED = "expired";
    public static final String STATUS_SUBMITTED = "submitted";
    public static final String STATUS_RESCORED = "rescored";
    public static final String STATUS_REPORTED = "reported";
    public static final String STATUS_CLOSED = "closed";
    public static final String STATUS_DISABLED = "disabled";
    
    // Mapping status codes to stages
    private static final Map<String, ExamStatusStage> statusToStage;
    static {
        Map<String, ExamStatusStage> stageMap = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);
        stageMap.put(STATUS_PAUSED, ExamStatusStage.INACTIVE);
        stageMap.put(STATUS_PENDING, ExamStatusStage.NEW);
        stageMap.put(STATUS_APPROVED, ExamStatusStage.NEW);
        stageMap.put(STATUS_SUSPENDED, ExamStatusStage.IN_PROGRESS);
        stageMap.put(STATUS_REVIEW, ExamStatusStage.IN_USE);
        stageMap.put(STATUS_STARTED, ExamStatusStage.IN_PROGRESS);
        stageMap.put(STATUS_DENIED, ExamStatusStage.INACTIVE);
        stageMap.put(STATUS_COMPLETED, ExamStatusStage.CLOSED);
        stageMap.put(STATUS_SCORED, ExamStatusStage.CLOSED);
        stageMap.put(STATUS_SEGMENT_ENTRY, ExamStatusStage.IN_USE);
        stageMap.put(STATUS_SEGMENT_EXIT, ExamStatusStage.IN_USE);
        stageMap.put(STATUS_INVALIDATED, ExamStatusStage.CLOSED);
        stageMap.put(STATUS_EXPIRED, ExamStatusStage.CLOSED);
        stageMap.put(STATUS_SUBMITTED, ExamStatusStage.CLOSED);
        stageMap.put(STATUS_RESCORED, ExamStatusStage.CLOSED);
        stageMap.put(STATUS_REPORTED, ExamStatusStage.CLOSED);
        stageMap.put(STATUS_CLOSED, ExamStatusStage.CLOSED);
        statusToStage = Collections.unmodifiableMap(stageMap);
    }
    
    private String code;
    private ExamStatusStage stage;

    public String getCode() {
        return code;
    }

    public ExamStatusStage getStage() {
        return stage;
    }

    /**
     * Private constructor for frameworks
     */
    private ExamStatusCode() {
    }
    
    public ExamStatusCode(String code) {
        this.code = code;
        this.stage = statusToStage.get(code);
        
        if (this.stage == null) {
            throw new IllegalArgumentException(String.format("No default stage found for status code %s", code));
        }
    }

    public ExamStatusCode(String code, ExamStatusStage stage) {
        this.code = code;
        this.stage = stage;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ExamStatusCode that = (ExamStatusCode) o;

        if (code != null ? !code.equals(that.code) : that.code != null) return false;
        return stage != null ? stage.equals(that.stage) : that.stage == null;

    }

    @Override
    public int hashCode() {
        int result = code != null ? code.hashCode() : 0;
        result = 31 * result + (stage != null ? stage.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "ExamStatusCode{" +
            "code='" + code + '\'' +
            ", stage='" + stage + '\'' +
            '}';
    }
}
