package tds.exam.health;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class HealthIndicatorTest {
    @Autowired
    private MockMvc mvc;

    @Test
    public void getHealthCheck() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/health").accept(MediaType.APPLICATION_JSON))
                .andExpect(content().string(containsString("status")));
    }
}
