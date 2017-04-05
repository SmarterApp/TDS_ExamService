package tds.exam.mappers;

import java.util.Set;
import java.util.UUID;

import tds.exam.ExpandableExamPrintRequest;

/**
 * Interface for a service that maps an {@link tds.exam.ExpandableExamPrintRequest} with optional attributes
 */
public interface ExpandableExamPrintRequestMapper {

    /**
     * Updates an {@link tds.exam.ExpandableExamPrintRequest.Builder} based on the expandable attributes provided
     *
     * @param expandableAttributes A set of optional exam print request attributes
     * @param builder              The builder of the {@link tds.exam.ExpandableExamPrintRequest}
     * @param examId               The id of the exam that the {@link tds.exam.ExamPrintRequest} belongs to
     */
    void updateExpandableMapper(final Set<String> expandableAttributes, final ExpandableExamPrintRequest.Builder builder,
                                final UUID examId);
}
