package tds.exam.services.impl;

import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import tds.common.web.exceptions.NotFoundException;
import tds.exam.Exam;
import tds.exam.ExamineeAttribute;
import tds.exam.ExamineeContext;
import tds.exam.ExamineeRelationship;
import tds.exam.repositories.ExamineeCommandRepository;
import tds.exam.services.ExamService;
import tds.exam.services.ExamineeService;
import tds.exam.services.StudentService;
import tds.student.Student;

@Service
public class ExamineeServiceImpl implements ExamineeService {
    private final ExamineeCommandRepository examineeCommandRepository;
    private final ExamService examService;
    private final StudentService studentService;

    @Autowired
    public ExamineeServiceImpl(ExamineeCommandRepository examineeCommandRepository,
                               ExamService examService,
                               StudentService studentService) {
        this.examineeCommandRepository = examineeCommandRepository;
        this.examService = examService;
        this.studentService = studentService;
    }

    @Override
    @Transactional
    public void insertAttributesAndRelationships(UUID examId, ExamineeContext context) {
        Exam exam = examService.findExam(examId)
            .orElseThrow(() -> new NotFoundException(String.format("Could not find exam for id %s", examId)));

        Student student = studentService.getStudentById(exam.getStudentId())
            .orElseThrow(() -> (new NotFoundException(String.format("Could not find student for exam id %s and student id %d", examId, exam.getStudentId()))));

        ExamineeAttribute[] examineeAttributes = student.getAttributes().stream()
            .map(attribute -> new ExamineeAttribute.Builder()
                .withExamId(examId)
                .withContext(context)
                .withName(attribute.getName())
                .withValue(attribute.getValue())
                .withCreatedAt(Instant.now())
                .build())
            .toArray(ExamineeAttribute[]::new);

        ExamineeRelationship[] examineeRelationships = student.getRelationships().stream()
            .map(relationship -> new ExamineeRelationship.Builder()
                .withExamId(examId)
                .withContext(context)
                .withName(relationship.getId())
                .withValue(relationship.getValue())
                .withType(relationship.getType())
                .withCreatedAt(Instant.now())
                .build())
            .toArray(ExamineeRelationship[]::new);

        examineeCommandRepository.insertAttributes(examineeAttributes);

        examineeCommandRepository.insertRelationships(examineeRelationships);
    }
}
