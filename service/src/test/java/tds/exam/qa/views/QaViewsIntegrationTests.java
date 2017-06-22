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

package tds.exam.qa.views;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest
public class QaViewsIntegrationTests {
    @Autowired
    @Qualifier("commandJdbcTemplate")
    private NamedParameterJdbcTemplate jdbcTemplate;


    @Test
    public void shouldQueryQaExamMostRecentEventPerExam() {
        queryView("qa_exam_most_recent_event_per_exam");
    }

    @Test
    public void shouldQueryQaExamMostRecentEventPerExamAccommodation() {
        queryView("qa_exam_most_recent_event_per_exam_accommodation");
    }

    @Test
    public void shouldQueryQaExamMostRecentEventPerExamPage() {
        queryView("qa_exam_most_recent_event_per_exam_page");
    }

    @Test
    public void shouldQueryQaExamMostRecentEventPerExamSegment() {
        queryView("qa_exam_most_recent_event_per_exam_segment");
    }

    @Test
    public void shouldQueryQaExamMostRecentResponsePerExamItem() {
        queryView("qa_exam_most_recent_response_per_exam_item");
    }

    @Test
    public void shouldQueryQaSessionTesteeAccommodations() {
        queryView("qa_session_testeeaccommodations");
    }

    @Test
    public void shouldQueryQaSessionTesteeAttribute() {
        queryView("qa_session_testeeattribute");
    }

    @Test
    public void shouldQueryQaSessionTesteeRelationship() {
        queryView("qa_session_testeerelationship");
    }

    @Test
    public void shouldQueryQaSessionTesteeResponse() {
        queryView("qa_session_testeeresponse");
    }

    @Test
    public void shouldQueryQaSessionTestOpportunity() {
        queryView("qa_session_testopportunity");
    }

    @Test
    public void shouldQueryQaSessionTestOpportunitySegment() {
        queryView("qa_session_testopportunitysegment");
    }

    @Test
    public void shouldQuerySessionTesteeComment() {
        queryView("qa_session_testeecomment");
    }

    private void queryView(final String viewName) {
        jdbcTemplate.query("select * from " + viewName + " limit 0, 1", resultSet -> "success");
    }
}
