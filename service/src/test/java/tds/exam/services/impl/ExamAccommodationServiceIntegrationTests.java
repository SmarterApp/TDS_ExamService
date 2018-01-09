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
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import tds.accommodation.Accommodation;
import tds.assessment.Assessment;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.builder.AccommodationBuilder;
import tds.exam.builder.AssessmentBuilder;
import tds.exam.builder.ExamBuilder;
import tds.exam.repositories.ExamAccommodationCommandRepository;
import tds.exam.repositories.ExamAccommodationQueryRepository;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.impl.ExamAccommodationCommandRepositoryImpl;
import tds.exam.services.AssessmentService;

import static java.util.Arrays.asList;
import static org.mockito.Mockito.when;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamAccommodationServiceIntegrationTests {
    private ExamAccommodationServiceImpl examAccommodationService;

    @Mock
    private ExamAccommodationQueryRepository mockExamAccommodationQueryRepository;

    @Mock
    private AssessmentService mockAssessmentService;

    @Autowired
    private ExamCommandRepository examCommandRepository;

    private Exam exam;

    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    private ExamAccommodationCommandRepository examAccommodationCommandRepository;


    @Before
    public void setUp() {
        exam = new ExamBuilder().build();
        examCommandRepository.insert(exam);

        examAccommodationCommandRepository = new ExamAccommodationCommandRepositoryImpl(jdbcTemplate);

        examAccommodationService = new ExamAccommodationServiceImpl(mockExamAccommodationQueryRepository,
            examAccommodationCommandRepository,
            mockAssessmentService);
    }

    @Test
    public void shouldInitializeTDSOtherAccommodationsOnPreviousExam() {
        Assessment assessment = new AssessmentBuilder().withKey(exam.getAssessmentId())
            .build();

        Accommodation accommodationTDS_Other = new AccommodationBuilder()
            .withCode("TDS_Other")
            .withType("Other")
            .withValue("test")
            .withSegmentPosition(0)
            .withEntryControl(false)
            .withAllowChange(true)
            .withSelectable(false)
            .build();

        String guestAccommodations = "ELA:TDS_Other#test";

        List<Accommodation> assessmentAccommodations = asList(accommodationTDS_Other);

        ExamAccommodation existingExamAccommodationTDS_Other = new ExamAccommodation.Builder(UUID.randomUUID())
            .withExamId(exam.getId())
            .withSegmentKey(UUID.randomUUID().toString()) // existing examination segment key exists
            .withCode("TDS_Other")
            .withType("Other")
            .withValue("test")
            .withDescription("test")
            .withSegmentPosition(0)
            .withCreatedAt(Instant.now())
            .withAllowChange(true)
            .withSelectable(false)
            .build();

        List<ExamAccommodation> existingExamAccommodation = asList(existingExamAccommodationTDS_Other);
        examAccommodationCommandRepository.insert(existingExamAccommodation);

        when(mockAssessmentService.findAssessmentAccommodationsByAssessmentKey(exam.getClientName(), assessment.getKey())).thenReturn(assessmentAccommodations);
        when(mockExamAccommodationQueryRepository.findAccommodations(exam.getId())).thenReturn(existingExamAccommodation);

        examAccommodationService.initializeAccommodationsOnPreviousExam(exam, assessment, 0, false, guestAccommodations);
    }
}
