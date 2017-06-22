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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import tds.exam.Exam;
import tds.exam.ExamineeNote;
import tds.exam.ExamineeNoteContext;
import tds.exam.builder.ExamBuilder;
import tds.exam.repositories.ExamCommandRepository;
import tds.exam.repositories.ExamineeNoteCommandRepository;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
@Transactional
public class ExamineeNoteCommandRepositoryImplIntegrationTests {
    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;

    private ExamCommandRepository examCommandRepository;
    private ExamineeNoteCommandRepository examineeNoteCommandRepository;

    private final Exam mockExam = new ExamBuilder().build();

    @Before
    public void setup() {
        examCommandRepository = new ExamCommandRepositoryImpl(jdbcTemplate);
        examineeNoteCommandRepository = new ExamineeNoteCommandRepositoryImpl(jdbcTemplate);
    }

    @Test
    public void shouldInsertAnExamineeNote() {
        ExamineeNote note = new ExamineeNote.Builder()
            .withExamId(mockExam.getId())
            .withContext(ExamineeNoteContext.ITEM)
            .withItemPosition(5)
            .withNote("exam item note")
            .build();

        examCommandRepository.insert(mockExam);

        examineeNoteCommandRepository.insert(note);
    }

    @Test(expected = DataIntegrityViolationException.class)
    public void shouldNotInsertAnExamineeNoteWhenTheExamIsNotPresent() {
        ExamineeNote note = new ExamineeNote.Builder()
            .withExamId(UUID.randomUUID())
            .withContext(ExamineeNoteContext.ITEM)
            .withItemPosition(5)
            .withNote("exam item note")
            .build();

        examineeNoteCommandRepository.insert(note);
    }
}
