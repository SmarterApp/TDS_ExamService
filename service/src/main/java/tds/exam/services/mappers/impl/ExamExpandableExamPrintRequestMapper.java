package tds.exam.services.mappers.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExpandableExamPrintRequest;
import tds.exam.services.ExamService;
import tds.exam.services.mappers.ExpandableExamPrintRequestMapper;

@Component
public class ExamExpandableExamPrintRequestMapper implements ExpandableExamPrintRequestMapper {
    private final ExamService examService;

    @Autowired
    public ExamExpandableExamPrintRequestMapper(final ExamService examService) {
        this.examService = examService;
    }

    @Override
    public void updateExpandableMapper(final Set<String> expandableAttributes, final ExpandableExamPrintRequest.Builder builder, final UUID examId) {
        if (expandableAttributes.contains(ExpandableExamPrintRequest.EXPANDABLE_PARAMS_PRINT_REQUEST_WITH_EXAM)) {
            Optional<Exam> maybeExam = examService.findExam(examId);

            if (!maybeExam.isPresent()) {
                throw new IllegalStateException("Could not retrieve an exam for the request exam print request");
            }

            builder.withExam(maybeExam.get());
        }
    }
}
