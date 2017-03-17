package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExpandableExamPrintRequest;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.services.ExpandableExamPrintRequestMapper;

@Component
public class ExamExpandableExamPrintRequestMapper implements ExpandableExamPrintRequestMapper {
    private final ExamQueryRepository examQueryRepository;

    @Autowired
    public ExamExpandableExamPrintRequestMapper(final ExamQueryRepository examQueryRepository) {
        this.examQueryRepository = examQueryRepository;
    }

    @Override
    public void updateExpandableMapper(final Set<String> expandableAttributes, final ExpandableExamPrintRequest.Builder builder, final UUID examId) {
        if (expandableAttributes.contains(ExpandableExamPrintRequest.EXPANDABLE_PARAMS_PRINT_REQUEST_WITH_EXAM)) {
            Optional<Exam> maybeExam = examQueryRepository.getExamById(examId);

            if (!maybeExam.isPresent()) {
                throw new IllegalStateException("Could not retrieve an exam for the request exam print request");
            }

            builder.withExam(maybeExam.get());
        }
    }
}
