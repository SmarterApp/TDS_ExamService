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


    private void queryView(final String viewName) {
        jdbcTemplate.query("select * from " + viewName + " limit 0, 1", resultSet -> "success");
    }
}
