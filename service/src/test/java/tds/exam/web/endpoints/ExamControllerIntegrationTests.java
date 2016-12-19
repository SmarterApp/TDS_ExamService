package tds.exam.web.endpoints;

import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import tds.common.web.advice.ExceptionAdvice;
import tds.exam.services.ExamService;

@RunWith(SpringRunner.class)
@WebMvcTest(ExamController.class)
@Import({ExceptionAdvice.class})
public class ExamControllerIntegrationTests {
    @Autowired
    private MockMvc http;

    @MockBean
    private ExamService examService;

    // @WebMvcTest is not wiring this up automatically (even though Spring does automatically wire it up when starting
    // the application normally).  As a work-around, this integration test class will mock the RestTemplateBuilder.
    // Some details about the RestTemplateBuilder can be found here:
    // https://docs.spring.io/spring-boot/docs/1.4.0.RELEASE/reference/html/boot-features-restclient.html
    @MockBean
    RestTemplateBuilder restTemplateBuilder;


}
