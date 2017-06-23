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

package tds.exam.web.endpoints;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import tds.exam.Exam;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.repositories.impl.ExamQueryRepositoryImpl;
import tds.exam.services.ExamApprovalService;
import tds.exam.services.SessionService;
import tds.exam.services.TimeLimitConfigurationService;
import tds.exam.services.impl.ExamApprovalServiceImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExamsPendingApprovalTest {

    private ExamApprovalController examApprovalController;

    private ExamApprovalService examApprovalService;

    private ExamQueryRepository examQueryRepository;

    @Mock
    private NamedParameterJdbcTemplate mockJdbcTemplate;

    @Mock
    private SessionService mockSessionService;

    @Mock
    private TimeLimitConfigurationService mockTimeLimitConfigurationService;

    // bypass java type erasure for mockJdbcTemplate
    interface ExamRowMapper extends RowMapper<Exam> {}

    @Before
    public void setUp() {
        HttpServletRequest request = new MockHttpServletRequest();
        ServletRequestAttributes requestAttributes = new ServletRequestAttributes(request);
        RequestContextHolder.setRequestAttributes(requestAttributes);

        examQueryRepository = new ExamQueryRepositoryImpl(mockJdbcTemplate);
        examApprovalService = new ExamApprovalServiceImpl(examQueryRepository, mockSessionService, mockTimeLimitConfigurationService);
        examApprovalController = new ExamApprovalController(examApprovalService);
    }

    @Test
    public void repositoryShouldGetExamsPendingApproval() {
        List<Exam> exams = Arrays.asList(mock(Exam.class));
        when(mockJdbcTemplate.query(anyString(), any(SqlParameterSource.class), any(ExamRowMapper.class))).thenReturn(exams);

        List<Exam> results = examQueryRepository.getExamsPendingApproval(UUID.randomUUID());

        assertThat(results).isNotNull().isEqualTo(exams);
    }

    @Test
    public void controllerShouldGetExamsPendingApproval() {
        List<Exam> exams = Arrays.asList(mock(Exam.class));
        when(mockJdbcTemplate.query(anyString(), any(SqlParameterSource.class), any(ExamRowMapper.class))).thenReturn(exams);

        ResponseEntity<List<Exam>> response = examApprovalController.getExamsPendingApproval(UUID.randomUUID());

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody().size()).isEqualTo(1);
    }
}
