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
