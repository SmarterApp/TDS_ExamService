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

package tds.exam.repositories.impl;

import org.joda.time.Instant;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import tds.exam.Exam;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.builder.ExamBuilder;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamStatusQueryRepository;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamStatusQueryRepositoryImplIntegrationTests {
    private ExamStatusQueryRepository examStatusQueryRepository;
    private ExamCommandRepository examCommandRepository;

    @Autowired
    @Qualifier("queryJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    @Before
    public void setUp() {
        examCommandRepository = new ExamCommandRepositoryImpl(jdbcTemplate);
        examStatusQueryRepository = new ExamStatusQueryRepositoryImpl(jdbcTemplate);
    }

    @Test
    public void shouldFindStatus() {
        //Statuses are current loaded via a migration script V1473962717__exam_create_status_codes_table.sql
        ExamStatusCode code = examStatusQueryRepository.findExamStatusCode("started");

        assertThat(code.getCode()).isEqualTo("started");
        assertThat(code.getStage()).isEqualTo(ExamStatusStage.IN_USE);
    }

    @Test
    public void shouldFindLastTimeStatusWasPaused() {
        Instant datePaused = Instant.now();
        Exam exam = new ExamBuilder()
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PAUSED), datePaused)
            .build();

        examCommandRepository.insert(exam);
        assertThat(examStatusQueryRepository.findRecentTimeAtStatus(exam.getId(), ExamStatusCode.STATUS_PAUSED).get()).isEqualTo(datePaused);
        Exam examRestarted = new Exam.Builder()
            .fromExam(exam)
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_STARTED), Instant.now())
            .build();

        examCommandRepository.update(examRestarted);
        assertThat(examStatusQueryRepository.findRecentTimeAtStatus(exam.getId(), ExamStatusCode.STATUS_PAUSED).get()).isEqualTo(datePaused);
        Instant datePausedAgain = Instant.now();
        Exam examPausedAgain = new Exam.Builder()
            .fromExam(exam)
            .withStatus(new ExamStatusCode(ExamStatusCode.STATUS_PAUSED), datePausedAgain)
            .build();

        examCommandRepository.update(examPausedAgain);
        assertThat(examStatusQueryRepository.findRecentTimeAtStatus(exam.getId(), ExamStatusCode.STATUS_PAUSED).get()).isEqualTo(datePausedAgain);
    }

    @Test(expected = EmptyResultDataAccessException.class)
    public void shouldThrowIfStatusNotFound() {
        examStatusQueryRepository.findExamStatusCode("bogus");
    }
}
