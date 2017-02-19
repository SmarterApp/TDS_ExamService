package tds.exam.services.impl;

import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import tds.common.web.exceptions.NotFoundException;
import tds.exam.Exam;
import tds.exam.ExamineeAttribute;
import tds.exam.ExamineeContext;
import tds.exam.ExamineeRelationship;
import tds.exam.repositories.ExamineeCommandRepository;
import tds.exam.services.ExamineeService;
import tds.exam.services.StudentService;
import tds.student.Student;

@Service
public class ExamineeServiceImpl implements ExamineeService {
    private final ExamineeCommandRepository examineeCommandRepository;
    private final StudentService studentService;

    @Autowired
    public ExamineeServiceImpl(final ExamineeCommandRepository examineeCommandRepository,
                               final StudentService studentService) {
        this.examineeCommandRepository = examineeCommandRepository;
        this.studentService = studentService;
    }

    @Override
    @Transactional
    public void insertAttributesAndRelationships(final Exam exam, final ExamineeContext context) {
        //A negative student id means it is a guest and attributes and relationships are not inserted because
        //test results are not sent to the scoring system
        if(exam.getStudentId() < 0) {
            return;
        }

        Student student = studentService.getStudentById(exam.getClientName(), exam.getStudentId())
            .orElseThrow(() -> new NotFoundException(String.format("Could not find student for exam id %s and student id %d", exam.getId(), exam.getStudentId())));

        insertAttributesAndRelationships(exam, student, context);
    }

    @Override
    @Transactional
    public void insertAttributesAndRelationships(final Exam exam, final Student student, final ExamineeContext context) {
        ExamineeAttribute[] examineeAttributes = student.getAttributes().stream()
            .map(attribute -> new ExamineeAttribute.Builder()
                .withExamId(exam.getId())
                .withContext(context)
                .withName(attribute.getName())
                .withValue(attribute.getValue())
                .withCreatedAt(Instant.now())
                .build())
            .toArray(ExamineeAttribute[]::new);

        ExamineeRelationship[] examineeRelationships = student.getRelationships().stream()
            .map(relationship -> new ExamineeRelationship.Builder()
                .withExamId(exam.getId())
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
