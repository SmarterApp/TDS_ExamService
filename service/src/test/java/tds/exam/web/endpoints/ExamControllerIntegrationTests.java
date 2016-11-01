package tds.exam.web.endpoints;

import java.net.URI;
import java.util.UUID;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.PlatformTransactionManager;

import tds.exam.services.AccommodationService;
import tds.exam.services.ExamService;

import static org.mockito.Mockito.verifyZeroInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringRunner.class)
@WebMvcTest(ExamController.class)
public class ExamControllerIntegrationTests {
    @Autowired
    private MockMvc http;

    @MockBean
    private AccommodationService accommodationService;

    @MockBean
    private ExamService examService;

    @MockBean
    private PlatformTransactionManager transactionManager;

    @MockBean
    private RestTemplateBuilder restTemplateBuilder;

    @Test
    public void shouldReturnBadRequestIfAccommodationTypesAreNotProvided() throws Exception {
        UUID mockExamId = UUID.randomUUID();
        http.perform(get(new URI(String.format("/exam/%s/unit-test-segment/accommodations", mockExamId)))
            .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isBadRequest());

        verifyZeroInteractions(accommodationService);
    }
}
