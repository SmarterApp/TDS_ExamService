package tds.exam.web.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamPrintRequest;
import tds.exam.services.ExamPrintRequestService;

@RestController
@RequestMapping("/exam/print")
public class ExamPrintRequestController {
    private final ExamPrintRequestService examPrintRequestService;

    @Autowired
    public ExamPrintRequestController(final ExamPrintRequestService examPrintRequestService) {
        this.examPrintRequestService = examPrintRequestService;
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createExamRequest(@RequestBody ExamPrintRequest request) {
        examPrintRequestService.insert(request);
    }

    @RequestMapping(value = "/approved/{sessionId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ExamPrintRequest>> findUnfulfilledRequests(@PathVariable UUID sessionId) {
        return ResponseEntity.ok(examPrintRequestService.findApprovedRequests(sessionId));
    }

    @RequestMapping(value = "/{sessionId}/{examId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<ExamPrintRequest>> findUnfulfilledRequests(final @PathVariable UUID examId, final @PathVariable UUID sessionId) {
        return ResponseEntity.ok(examPrintRequestService.findUnfulfilledRequests(examId, sessionId));
    }

    @RequestMapping(value = "/deny/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void findUnfulfilledRequests(final @PathVariable UUID id, final @RequestBody String reason) {
        examPrintRequestService.denyRequest(id, reason);
    }

    @RequestMapping(value = "/approve/{id}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExamPrintRequest> findAndApprovePrintRequest(final @PathVariable UUID id) {
        Optional<ExamPrintRequest> maybeExamPrintRequest = examPrintRequestService.findAndApprovePrintRequest(id);

        if (!maybeExamPrintRequest.isPresent()) {
            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ResponseEntity.ok(maybeExamPrintRequest.get());
    }
}
