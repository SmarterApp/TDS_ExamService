package tds.exam.web.endpoints;

import com.jayway.restassured.http.ContentType;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import tds.exam.ExamServiceApplication;

import static com.jayway.restassured.RestAssured.given;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = ExamServiceApplication.class)
@WebAppConfiguration
@IntegrationTest("server.port:8080")
public class HealthControllerIntegrationTests {
    @Test
    public void shouldReturnOk() {
        given()
            .accept(ContentType.JSON)
        .when()
            .get("/exam/isAlive")
        .then()
            .contentType(ContentType.JSON)
            .statusCode(200);
    }
}
