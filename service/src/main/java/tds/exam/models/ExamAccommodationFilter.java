package tds.exam.models;

import static tds.common.util.Preconditions.checkNotNull;

/**
 * A way to filter exam accommodations
 */
public class ExamAccommodationFilter {
    private final String code;
    private final String type;

    public ExamAccommodationFilter(final String code, final String type) {
        this.code = checkNotNull(code);
        this.type = checkNotNull(type);
    }

    /**
     * @return the accommodation code
     */
    public String getCode() {
        return code;
    }

    /**
     * @return the accommodation type
     */
    public String getType() {
        return type;
    }
}
