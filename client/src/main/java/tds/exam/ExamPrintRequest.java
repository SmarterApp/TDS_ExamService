package tds.exam;

import org.joda.time.Instant;

import java.util.UUID;

/**
 * Represents a request to print or emboss and exam item, passage, or page.
 */
public class ExamPrintRequest {
    public static final String REQUEST_TYPE_EMBOSS_ITEM = "EMBOSSITEM";
    public static final String REQUEST_TYPE_EMBOSS_PASSAGE = "EMBOSSPASSAGE";
    public static final String REQUEST_TYPE_EMBOSS_PAGE = "EMBOSSPAGE";
    public static final String REQUEST_TYPE_PRINT_ITEM = "PRINTITEM";
    public static final String REQUEST_TYPE_PRINT_PASSAGE = "PRINTPASSAGE";
    public static final String REQUEST_TYPE_PRINT_PAGE = "PRINTPAGE";

    private UUID id;
    private UUID examId;
    private UUID sessionId;
    private String type;
    private String value;
    private int itemPosition;
    private int pagePosition;
    private String parameters;
    private String description;
    private Instant createdAt;
    private Instant changedAt;
    private ExamPrintRequestStatus status;
    private String reasonDenied;

    private ExamPrintRequest() {
    }

    public ExamPrintRequest(Builder builder) {
        this.createdAt = builder.createdAt;
        this.pagePosition = builder.pagePosition;
        this.itemPosition = builder.itemPosition;
        this.changedAt = builder.changedAt;
        this.examId = builder.examId;
        this.parameters = builder.parameters;
        this.status = builder.status;
        this.value = builder.value;
        this.id = builder.id;
        this.sessionId = builder.sessionId;
        this.type = builder.type;
        this.reasonDenied = builder.reasonDenied;
        this.description = builder.description;
    }

    public static final class Builder {
        private UUID id;
        private UUID examId;
        private UUID sessionId;
        private String type;
        private String value;
        private int itemPosition;
        private int pagePosition;
        private String parameters;
        private String description;
        private Instant createdAt;
        private Instant changedAt;
        private ExamPrintRequestStatus status;
        private String reasonDenied;

        public Builder(UUID id) {
            this.id = id;
        }

        public Builder withExamId(UUID examId) {
            this.examId = examId;
            return this;
        }

        public Builder withSessionId(UUID sessionId) {
            this.sessionId = sessionId;
            return this;
        }

        public Builder withType(String type) {
            this.type = type;
            return this;
        }

        public Builder withValue(String value) {
            this.value = value;
            return this;
        }

        public Builder withItemPosition(int itemPosition) {
            this.itemPosition = itemPosition;
            return this;
        }

        public Builder withPagePosition(int pagePosition) {
            this.pagePosition = pagePosition;
            return this;
        }

        public Builder withParameters(String parameters) {
            this.parameters = parameters;
            return this;
        }

        public Builder withDescription(String description) {
            this.description = description;
            return this;
        }

        public Builder withCreatedAt(Instant createdAt) {
            this.createdAt = createdAt;
            return this;
        }

        public Builder withChangedAt(Instant changedAt) {
            this.changedAt = changedAt;
            return this;
        }

        public Builder withStatus(ExamPrintRequestStatus status) {
            this.status = status;
            return this;
        }

        public Builder withReasonDenied(String reasonDenied) {
            this.reasonDenied = reasonDenied;
            return this;
        }

        public Builder fromExamPrintRequest(ExamPrintRequest request) {
            this.createdAt = request.createdAt;
            this.pagePosition = request.pagePosition;
            this.itemPosition = request.itemPosition;
            this.changedAt = request.changedAt;
            this.examId = request.examId;
            this.parameters = request.parameters;
            this.status = request.status;
            this.value = request.value;
            this.id = request.id;
            this.sessionId = request.sessionId;
            this.type = request.type;
            this.reasonDenied = request.reasonDenied;
            this.description = request.description;
            return this;
        }

        public ExamPrintRequest build() {
            return new ExamPrintRequest(this);
        }
    }

    /**
     * @return the id of the {@link tds.exam.ExamPrintRequest}
     */
    public UUID getId() {
        return id;
    }

    /**
     * @return the id of the exam requesting a print or embossing
     */
    public UUID getExamId() {
        return examId;
    }

    /**
     * @return the id of the session the exam belongs to that is request a print or emboss
     */
    public UUID getSessionId() {
        return sessionId;
    }

    /**
     * @return The type of print request (embossing item, embossing passing, print item, print passage, etc.)
     */
    public String getType() {
        return type;
    }

    /**
     * @return The value of the request - the path(s) to the requested resource
     */
    public String getValue() {
        return value;
    }

    /**
     * @return The position of the item that is being requested for print or embossing
     */
    public int getItemPosition() {
        return itemPosition;
    }

    /**
     * @return The position of the page that is being requested for print or embossing
     */
    public int getPagePosition() {
        return pagePosition;
    }

    /**
     * @return A string representing additional parameter(s) for the request
     */
    public String getParameters() {
        return parameters;
    }

    /**
     * @return The description of the request
     */
    public String getDescription() {
        return description;
    }

    /**
     * @return The {@link org.joda.time.Instant} the request was submitted
     */
    public Instant getCreatedAt() {
        return createdAt;
    }

    /**
     * @return The {@link org.joda.time.Instant} of when the {@link tds.exam.ExamPrintRequestStatus} last changed
     */
    public Instant getChangedAt() {
        return changedAt;
    }

    /**
     * @return The {@link tds.exam.ExamPrintRequestStatus} of the {@link tds.exam.ExamPrintRequest}
     */
    public ExamPrintRequestStatus getStatus() {
        return status;
    }

    /**
     * @return If denied, a description of why the {@link tds.exam.ExamPrintRequest} was denied, provided by the proctor
     */
    public String getReasonDenied() {
        return reasonDenied;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        final ExamPrintRequest that = (ExamPrintRequest) o;

        if (itemPosition != that.itemPosition) return false;
        if (pagePosition != that.pagePosition) return false;
        if (!examId.equals(that.examId)) return false;
        if (!sessionId.equals(that.sessionId)) return false;
        if (!type.equals(that.type)) return false;
        if (value != null ? !value.equals(that.value) : that.value != null) return false;
        if (parameters != null ? !parameters.equals(that.parameters) : that.parameters != null) return false;
        if (description != null ? !description.equals(that.description) : that.description != null) return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        return reasonDenied != null ? reasonDenied.equals(that.reasonDenied) : that.reasonDenied == null;
    }

    @Override
    public int hashCode() {
        int result = examId.hashCode();
        result = 31 * result + sessionId.hashCode();
        result = 31 * result + type.hashCode();
        result = 31 * result + (value != null ? value.hashCode() : 0);
        result = 31 * result + itemPosition;
        result = 31 * result + pagePosition;
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (changedAt != null ? changedAt.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (reasonDenied != null ? reasonDenied.hashCode() : 0);
        return result;
    }
}
