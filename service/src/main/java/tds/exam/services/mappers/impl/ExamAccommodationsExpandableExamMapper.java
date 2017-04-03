package tds.exam.services.mappers.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.exam.ExamAccommodation;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamParameters;
import tds.exam.services.ExamAccommodationService;
import tds.exam.services.mappers.ExpandableExamMapper;

@Component
public class ExamAccommodationsExpandableExamMapper implements ExpandableExamMapper {
    private final ExamAccommodationService examAccommodationService;

    @Autowired
    public ExamAccommodationsExpandableExamMapper(final ExamAccommodationService examAccommodationService) {
        this.examAccommodationService = examAccommodationService;
    }

    @Override
    public void updateExpandableMapper(final Set<ExpandableExamParameters> expandableExamAttributes, final Map<UUID, ExpandableExam.Builder> examBuilders, final UUID sessionId) {
        if (!expandableExamAttributes.contains(ExpandableExamParameters.EXPANDABLE_PARAMS_EXAM_ACCOMMODATIONS)) {
            return;
        }

        List<ExamAccommodation> examAccommodations =
            examAccommodationService.findApprovedAccommodations(examBuilders.keySet().toArray(new UUID[examBuilders.size()]));

        // list exam accoms grouped by the examId
        Map<UUID, List<ExamAccommodation>> sortedAccommodations = examAccommodations.stream()
            .collect(Collectors.groupingBy(ExamAccommodation::getExamId));

        // Assign each sub-list of exam accommodations to their respective exam ids
        sortedAccommodations.forEach((examId, sortedExamAccommodations) -> {
            ExpandableExam.Builder builder = examBuilders.get(examId);
            builder.withExamAccommodations(sortedExamAccommodations);
        });
    }
}
