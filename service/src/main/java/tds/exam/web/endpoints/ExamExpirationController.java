package tds.exam.web.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import tds.exam.ExpiredExamResponse;
import tds.exam.services.ExamExpirationService;

@RestController
@RequestMapping("/exam")
public class ExamExpirationController {
    private final ExamExpirationService examExpirationService;

    @Autowired
    public ExamExpirationController(final ExamExpirationService examExpirationService) {
        this.examExpirationService = examExpirationService;
    }

    @PostMapping(value = "expire/{clientName}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ExpiredExamResponse> expireExams(@PathVariable  final String clientName) {
        return ResponseEntity.ok(examExpirationService.expireExams(clientName));
    }
}
