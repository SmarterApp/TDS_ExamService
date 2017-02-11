package tds.exam.repositories.impl;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import tds.exam.Exam;
import tds.exam.ExamineeAttribute;
import tds.exam.ExamineeContext;
import tds.exam.ExamineeRelationship;
import tds.exam.builder.ExamBuilder;
import tds.exam.builder.ExamineeAttributeBuilder;
import tds.exam.builder.ExamineeRelationshipBuilder;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamineeCommandRepository;
import tds.exam.repositories.ExamineeQueryRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamineeCommandRepositoryImplIntegrationTests {
    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    private ExamineeCommandRepository examineeCommandRepository;
    private ExamineeQueryRepository examineeQueryRepository;
    private ExamCommandRepository examCommandRepository;
    private Exam mockExam = new ExamBuilder().build();

    @Before
    public void setUp() {
        examineeCommandRepository = new ExamineeCommandRepositoryImpl(jdbcTemplate);
        examineeQueryRepository = new ExamineeQueryRepositoryImpl(jdbcTemplate);

        examCommandRepository = new ExamCommandRepositoryImpl(jdbcTemplate);
    }

    @Test
    public void shouldInsertExamineeAttributes() {
        examCommandRepository.insert(mockExam);

        ExamineeAttribute firstAttribute = new ExamineeAttributeBuilder()
            .withExamId(mockExam.getId())
            .withContext(ExamineeContext.INITIAL)
            .build();
        ExamineeAttribute secondAttribute = new ExamineeAttributeBuilder()
            .withExamId(mockExam.getId())
            .withContext(ExamineeContext.INITIAL)
            .withName("AnotherUnitTest")
            .withValue("AnotherUnitTestValue")
            .build();

        examineeCommandRepository.insertAttributes(firstAttribute, secondAttribute);

        List<ExamineeAttribute> savedAttributes = examineeQueryRepository.findAllAttributes(mockExam.getId(),
            ExamineeContext.INITIAL);

        assertThat(savedAttributes).hasSize(2);
        ExamineeAttribute firstSavedAttribute = savedAttributes.get(0);
        assertThat(firstSavedAttribute.getId()).isGreaterThan(0L);
        assertThat(firstSavedAttribute.getExamId()).isEqualTo(mockExam.getId());
        assertThat(firstSavedAttribute.getContext()).isEqualTo(firstAttribute.getContext());
        assertThat(firstSavedAttribute.getName()).isEqualTo(firstAttribute.getName());
        assertThat(firstSavedAttribute.getValue()).isEqualTo(firstAttribute.getValue());

        ExamineeAttribute secondSavedAttribute = savedAttributes.get(1);
        assertThat(secondSavedAttribute.getId()).isGreaterThan(0L);
        assertThat(secondSavedAttribute.getExamId()).isEqualTo(mockExam.getId());
        assertThat(secondSavedAttribute.getContext()).isEqualTo(secondAttribute.getContext());
        assertThat(secondSavedAttribute.getName()).isEqualTo(secondAttribute.getName());
        assertThat(secondSavedAttribute.getValue()).isEqualTo(secondAttribute.getValue());
    }

    @Test
    public void shouldInsertExamineeRelationships() {
        examCommandRepository.insert(mockExam);

        ExamineeRelationship firstRelationship = new ExamineeRelationshipBuilder()
            .withExamId(mockExam.getId())
            .build();
        ExamineeRelationship secondRelationship = new ExamineeRelationshipBuilder()
            .withExamId(mockExam.getId())
            .withName("AnotherUnitTestName")
            .withValue("AnotherUnitTestValue")
            .withType("AnotherUnitTestType")
            .build();

        examineeCommandRepository.insertRelationships(firstRelationship, secondRelationship);

        List<ExamineeRelationship> savedRelationships = examineeQueryRepository.findAllRelationships(mockExam.getId(),
            ExamineeContext.INITIAL);

        assertThat(savedRelationships).hasSize(2);
        ExamineeRelationship firstSavedRelationship = savedRelationships.get(0);
        assertThat(firstSavedRelationship.getId()).isGreaterThan(0L);
        assertThat(firstSavedRelationship.getExamId()).isEqualTo(mockExam.getId());
        assertThat(firstSavedRelationship.getContext()).isEqualTo(firstRelationship.getContext());
        assertThat(firstSavedRelationship.getName()).isEqualTo(firstRelationship.getName());
        assertThat(firstSavedRelationship.getValue()).isEqualTo(firstRelationship.getValue());
        assertThat(firstSavedRelationship.getType()).isEqualTo(firstRelationship.getType());

        ExamineeRelationship secondSavedRelationship = savedRelationships.get(1);
        assertThat(secondSavedRelationship.getId()).isGreaterThan(0L);
        assertThat(secondSavedRelationship.getExamId()).isEqualTo(mockExam.getId());
        assertThat(secondSavedRelationship.getContext()).isEqualTo(secondRelationship.getContext());
        assertThat(secondSavedRelationship.getName()).isEqualTo(secondRelationship.getName());
        assertThat(secondSavedRelationship.getValue()).isEqualTo(secondRelationship.getValue());
        assertThat(secondSavedRelationship.getType()).isEqualTo(secondRelationship.getType());
    }
}
