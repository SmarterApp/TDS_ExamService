package tds.exam;

import java.util.UUID;

/**
 * An accommodation that is approved for use during an {@link Exam}.
 */
public class Accommodation {
    private Long id;
    private UUID examId;
    // TODO: Change to whatever type the segment id is.  Maybe the object?
    private int segmentId;
    private String type;
    private String code;

    public Accommodation(Long id, UUID examId, int segmentId, String type, String code) {
        if (id == null) {
            throw new IllegalArgumentException("id cannot be null");
        }

        if (examId == null) {
            throw new IllegalArgumentException("examId cannot be null");
        }

        if (type == null) {
            throw new IllegalArgumentException("type cannot be null");
        }

        if (code == null) {
            throw new IllegalArgumentException("code cannot be null");
        }

        this.id = id;
        this.examId = examId;
        this.segmentId = segmentId;
        this.type = type;
        this.code = code;
    }

    /**
     * @return The unique identifier of the {@link Accommodation} record
     */
    public Long getId() {
        return id;
    }

    /**
     * @return The id of the {@link Exam} to which this {@link Accommodation} belongs
     */
    public UUID getExamId() {
        return examId;
    }

    /**
     * @return The segment of the Assessment in which this {@link Accommodation} can be used
     */
    public int getSegmentId() {
        return segmentId;
    }

    /**
     * @return The type of this {@link Accommodation}
     */
    public String getType() {
        return type;
    }

    /**
     * @return The code for this {@link Accommodation}
     */
    public String getCode() {
        return code;
    }
}
