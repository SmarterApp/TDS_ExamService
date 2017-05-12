package tds.exam.mappers.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamAttributes;
import tds.exam.mappers.ExpandableExamMapper;
import tds.exam.services.ExamService;

@Component
public class WindowAttemptsExpandableExamMapper implements ExpandableExamMapper {
    private final ExamService examService;

    @Autowired
    public WindowAttemptsExpandableExamMapper(final ExamService examService) {
        this.examService = examService;
    }

    @Override
    public void updateExpandableMapper(final Set<ExpandableExamAttributes> expandableAttributes, final Map<UUID, ExpandableExam.Builder> examBuilders, final UUID sessionId) {
        if (!expandableAttributes.contains(ExpandableExamAttributes.WINDOW_ATTEMPTS)) {
            return;
        }

        examBuilders.forEach((examId, builder) -> {
            Exam exam = examService.findExam(examId)
                .orElseThrow(() -> new IllegalArgumentException(String.format("Cannot find exam for examId %s", examId)));
            List<Exam> exams = examService.findAllExamsForStudent(exam.getStudentId());
            // Get the count of all exams within this window, with the same assessment/client
            builder.withWindowAttempts((int) exams.stream()
                .filter(e -> e.getAssessmentKey().equals(exam.getAssessmentKey())
                    && e.getAssessmentWindowId().equals(exam.getAssessmentWindowId())
                    && e.getClientName().equals(exam.getClientName()))
                .count()
            );
        });
    }
}
