package tds.exam.web.endpoints;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;

import java.net.URI;

import tds.common.configuration.JacksonObjectMapperConfiguration;
import tds.common.configuration.SecurityConfiguration;
import tds.common.web.advice.ExceptionAdvice;
import tds.exam.ExamPrintRequest;
import tds.exam.services.ExamPrintRequestService;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ExamPrintRequestController.class)
@Import({ExceptionAdvice.class, JacksonObjectMapperConfiguration.class, SecurityConfiguration.class})
public class ExamPrintRequestControllerIntegrationTests {
    @Autowired
    private MockMvc http;

    @MockBean
    private ExamPrintRequestService mockExamPrintRequestService;

    @Autowired
    ObjectMapper objectMapper;

    @Test
    public void shouldCreateExamPrintRequest() throws Exception {
        ExamPrintRequest printRequest = random(ExamPrintRequest.class);
        ObjectWriter ow = objectMapper
            .writer().withDefaultPrettyPrinter();

        http.perform(post(new URI("/exam/print"))
            .contentType(MediaType.APPLICATION_JSON)
            .content(ow.writeValueAsString(printRequest)))
            .andExpect(status().isNoContent());

        verify(mockExamPrintRequestService).insert(any());
    }
}
