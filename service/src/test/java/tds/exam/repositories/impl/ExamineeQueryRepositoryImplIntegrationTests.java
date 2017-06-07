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
import java.util.UUID;

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
public class ExamineeQueryRepositoryImplIntegrationTests {
    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    private ExamCommandRepository examCommandRepository;
    private ExamineeCommandRepository examineeCommandRepository;
    private ExamineeQueryRepository examineeQueryRepository;

    private final UUID examId = UUID.randomUUID();

    @Before
    public void setUp() {
        examineeCommandRepository = new ExamineeCommandRepositoryImpl(jdbcTemplate);
        examineeQueryRepository = new ExamineeQueryRepositoryImpl(jdbcTemplate);
        examCommandRepository = new ExamCommandRepositoryImpl(jdbcTemplate);
    }

    @Test
    public void shouldFetchTheMostRecentExamineeAttributeValue() {
        examCommandRepository.insert(new ExamBuilder().withId(examId).build());

        ExamineeAttribute attribute = new ExamineeAttributeBuilder()
            .withExamId(examId)
            .withValue("1 attribute value")
            .build();
        ExamineeAttribute moreRecentAttribute = new ExamineeAttributeBuilder()
            .withExamId(examId)
            .withValue("2 attribute value")
            .build();

        examineeCommandRepository.insertAttributes(attribute, moreRecentAttribute);

        List<ExamineeAttribute> result = examineeQueryRepository.findAllAttributes(examId, ExamineeContext.INITIAL);

        assertThat(result).hasSize(1);
        ExamineeAttribute mostRecentAttribute = result.get(0);
        assertThat(mostRecentAttribute.getValue()).isEqualTo(moreRecentAttribute.getValue());
    }

    @Test
    public void shouldFetchAllAttributes() {
        examCommandRepository.insert(new ExamBuilder().withId(examId).build());

        ExamineeAttribute attribute1 = new ExamineeAttributeBuilder()
            .withId(1)
            .withExamId(examId)
            .withValue("1 attribute value")
            .withContext(ExamineeContext.INITIAL)
            .build();
        ExamineeAttribute attribute2 = new ExamineeAttributeBuilder()
            .withId(2)
            .withExamId(examId)
            .withValue("2 attribute value")
            .withContext(ExamineeContext.FINAL)
            .build();

        examineeCommandRepository.insertAttributes(attribute1, attribute2);

        List<ExamineeAttribute> result = examineeQueryRepository.findAllAttributes(examId);

        assertThat(result).hasSize(2);
        ExamineeAttribute retAttribute1 = null;
        ExamineeAttribute retAttribute2 = null;

        for (ExamineeAttribute a : result) {
            if (a.getValue().equals(attribute1.getValue())) {
                retAttribute1 = a;
            } else if (a.getValue().equals(attribute2.getValue())) {
                retAttribute2 = a;
            }
        }

        assertThat(retAttribute1.getCreatedAt()).isNotNull();
        assertThat(retAttribute1).isNotNull();
        assertThat(retAttribute2).isNotNull();
        assertThat(retAttribute1.getCreatedAt()).isNotNull();
    }

    @Test
    public void shouldFetchAllRelationships() {
        examCommandRepository.insert(new ExamBuilder().withId(examId).build());

        ExamineeRelationship relationship1 = new ExamineeRelationshipBuilder()
            .withId(1)
            .withExamId(examId)
            .withValue("1 relationship value")
            .withContext(ExamineeContext.INITIAL)
            .build();
        ExamineeRelationship relationship2 = new ExamineeRelationshipBuilder()
            .withId(2)
            .withExamId(examId)
            .withValue("2 relationship value")
            .withContext(ExamineeContext.FINAL)
            .build();

        examineeCommandRepository.insertRelationships(relationship1, relationship2);

        List<ExamineeRelationship> result = examineeQueryRepository.findAllRelationships(examId);

        assertThat(result).hasSize(2);
        ExamineeRelationship retRelationship1 = null;
        ExamineeRelationship retRelationship2 = null;

        for (ExamineeRelationship r : result) {
            if (r.getValue().equals(relationship1.getValue())) {
                retRelationship1 = r;
            } else if (r.getValue().equals(relationship2.getValue())) {
                retRelationship2 = r;
            }
        }

        assertThat(retRelationship1.getCreatedAt()).isNotNull();
        assertThat(retRelationship1).isNotNull();
        assertThat(retRelationship2).isNotNull();
        assertThat(retRelationship2.getCreatedAt()).isNotNull();
    }

    @Test
    public void shouldFetchTheMostRecentExamineeRelationshipValue() {
        examCommandRepository.insert(new ExamBuilder().withId(examId).build());

        ExamineeRelationship relationship = new ExamineeRelationshipBuilder()
            .withExamId(examId)
            .withValue("1 relationship value")
            .build();
        ExamineeRelationship moreRecentRelationship = new ExamineeRelationshipBuilder()
            .withExamId(examId)
            .withValue("2 relationship value")
            .build();

        examineeCommandRepository.insertRelationships(relationship, moreRecentRelationship);

        List<ExamineeRelationship> result = examineeQueryRepository.findAllRelationships(examId,
            ExamineeContext.INITIAL);

        assertThat(result).hasSize(1);
        ExamineeRelationship mostRecentRelationship = result.get(0);
        assertThat(mostRecentRelationship.getValue()).isEqualTo(moreRecentRelationship.getValue());
    }
}
