package tds.exam.web.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import tds.common.Response;
import tds.exam.ApprovalRequest;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.services.ExamApprovalService;

@RestController
@RequestMapping("/exam")
public class ExamApprovalController {
    private final ExamApprovalService examApprovalService;

    @Autowired
    public ExamApprovalController(ExamApprovalService examApprovalService) {
        this.examApprovalService = examApprovalService;
    }

    @RequestMapping(value = "/{id}/approval", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Response<ExamApproval>> getApproval(@PathVariable final UUID id,
                                                       @RequestParam final UUID sessionId,
                                                       @RequestParam final UUID browserId) {
        ApprovalRequest approvalRequest = new ApprovalRequest(id, sessionId, browserId);
        Response<ExamApproval> examApproval = examApprovalService.getApproval(approvalRequest);

        if (examApproval.hasError()) {
            return new ResponseEntity<>(examApproval, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ResponseEntity.ok(examApproval);
    }

    @RequestMapping(value = "/pending-approval/{sessionId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<Exam>> getExamsPendingApproval(@PathVariable final UUID sessionId) {
        List<Exam> examsPendingApproval = examApprovalService.getExamsPendingApproval(sessionId);

        return ResponseEntity.ok(examsPendingApproval);
    }
}
