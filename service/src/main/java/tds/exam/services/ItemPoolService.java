package tds.exam.services;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import tds.assessment.Item;
import tds.assessment.ItemConstraint;

/**
 * A service used for selecting items for exam segments.
 */
public interface ItemPoolService {
    /**
     * Retrieves a collection of eligible items for the exam segment based on exam accommodations and assessment
     * item constraints.
     *
     * @param examId    the id of the {@link tds.exam.Exam}
     * @param itemConstraints    the {@link tds.assessment.ItemConstraint}s for the assessment
     * @param items     the collection of all possible {@link tds.assessment.Item}s in a {@link tds.assessment.Segment}
     * @return  returns a filtered list of {@link tds.assessment.Item}s eligible for the segment pool
     */
    Set<Item> getItemPool(UUID examId, List<ItemConstraint> itemConstraints, List<Item> items);

    /**
     * Retrieves a collection of eligible items for the exam segment based on exam accommodations and assessment
     * item constraints.
     *
     * @param examId          the id of the {@link tds.exam.Exam}
     * @param itemConstraints the {@link tds.assessment.ItemConstraint}s for the assessment
     * @param items           the collection of all possible {@link tds.assessment.Item}s in a {@link tds.assessment.Segment}
     * @param isFieldTest     filter by field test items
     * @return returns a filtered list of {@link tds.assessment.Item}s eligible for the segment pool
     */
    Set<Item> getItemPool(UUID examId, List<ItemConstraint> itemConstraints, List<Item> items, Boolean isFieldTest);
}
