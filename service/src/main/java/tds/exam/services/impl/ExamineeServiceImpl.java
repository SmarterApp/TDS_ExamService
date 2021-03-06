/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

package tds.exam.services.impl;

import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import tds.common.web.exceptions.NotFoundException;
import tds.exam.Exam;
import tds.exam.ExamineeAttribute;
import tds.exam.ExamineeContext;
import tds.exam.ExamineeRelationship;
import tds.exam.repositories.ExamineeCommandRepository;
import tds.exam.repositories.ExamineeQueryRepository;
import tds.exam.services.ExamineeService;
import tds.exam.services.StudentService;
import tds.student.Student;

@Service
public class ExamineeServiceImpl implements ExamineeService {
    private final ExamineeCommandRepository examineeCommandRepository;
    private final ExamineeQueryRepository examineeQueryRepository;
    private final StudentService studentService;

    @Autowired
    public ExamineeServiceImpl(final ExamineeCommandRepository examineeCommandRepository,
                               final ExamineeQueryRepository examineeQueryRepository,
                               final StudentService studentService) {
        this.examineeCommandRepository = examineeCommandRepository;
        this.examineeQueryRepository = examineeQueryRepository;
        this.studentService = studentService;
    }

    @Override
    public List<ExamineeAttribute> findAllAttributes(final UUID examId) {
        return examineeQueryRepository.findAllAttributes(examId);
    }

    @Override
    public List<ExamineeRelationship> findAllRelationships(final UUID examId) {
        return examineeQueryRepository.findAllRelationships(examId);
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
