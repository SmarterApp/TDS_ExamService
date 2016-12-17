package tds.exam.services;

import java.util.List;
import java.util.UUID;

import tds.assessment.AdaptiveSegment;
import tds.assessment.ItemConstraint;
import tds.assessment.Segment;
import tds.exam.Exam;
import tds.exam.models.SegmentPoolInfo;

/**
 * Service responsible for selecting segment items and gathering segment pool information.
 */
public interface SegmentPoolService {
    /**
     * A {@link tds.exam.models.SegmentPoolInfo} object containing metadata about the selected segment pool.
     *
     * @param examId          The id of the {@link Exam}
     * @param segment         The segment being constructed
     * @param itemConstraints The list of assessment item constraints
     * @param languageCode    The language of the exam
     * @return The {@link tds.exam.models.SegmentPoolInfo} containing segment pool information
     */
    SegmentPoolInfo computeSegmentPool(UUID examId, Segment segment, List<ItemConstraint> itemConstraints, String languageCode);
}
