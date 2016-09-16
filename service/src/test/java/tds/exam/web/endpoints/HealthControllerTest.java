package tds.exam.web.endpoints;

import org.junit.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

public class HealthControllerTest {
    @Test
    public void aliveShouldReturnOk() {
        HealthController controller = new HealthController();
        ResponseEntity<String> response = controller.isAlive();
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo("Exam Service Alive");
    }
}
