package tds.exam.web.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.common.web.exceptions.NotFoundException;
import tds.common.web.resources.NoContentResponseResource;
import tds.exam.ApprovalRequest;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.ExamConfiguration;
import tds.exam.OpenExamRequest;
import tds.exam.services.ExamService;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;

@RestController
@RequestMapping("/exam")
public class ExamController {
    private final ExamService examService;

    @Autowired
    public ExamController(ExamService examService) {
        this.examService = examService;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<Exam> getExamById(@PathVariable UUID id) {
        final Exam exam = examService.findExam(id)
            .orElseThrow(() -> new NotFoundException("Could not find exam for %s", id));

        return ResponseEntity.ok(exam);
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Response<Exam>> openExam(@RequestBody final OpenExamRequest openExamRequest) {
        Response<Exam> exam = examService.openExam(openExamRequest);

        if (!exam.getData().isPresent()) {
            return new ResponseEntity<>(exam, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Link link = linkTo(
            methodOn(ExamController.class)
                .getExamById(exam.getData().get().getId()))
            .withSelfRel();

        final HttpHeaders headers = new HttpHeaders();
        headers.add("Location", link.getHref());
        return new ResponseEntity<>(exam, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/{examId}/start", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Response<ExamConfiguration>> startExam(@PathVariable final UUID examId) {
        Response<ExamConfiguration> examConfiguration = examService.startExam(examId);

        if (examConfiguration.getErrors().isPresent()) {
            return new ResponseEntity<>(examConfiguration, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ResponseEntity.ok(examConfiguration);
    }

    @RequestMapping(value = "/{id}/get-approval", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Response<ExamApproval>> getApproval(@PathVariable final UUID examId, @RequestParam final UUID sessionId, @RequestParam final UUID browserId, final String clientName) {
        ApprovalRequest approvalRequest = new ApprovalRequest(examId, sessionId, browserId, clientName);
        Response<ExamApproval> examApproval = examService.getApproval(approvalRequest);

        if (examApproval.getErrors().isPresent()) {
            return new ResponseEntity<>(examApproval, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ResponseEntity.ok(examApproval);
    }

    @RequestMapping(value = "/{examId}/pause", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<NoContentResponseResource> pauseExam(@PathVariable final UUID examId) {
        final Optional<ValidationError> maybeStatusTransitionFailure = examService.pauseExam(examId);

        if (maybeStatusTransitionFailure.isPresent()) {
            NoContentResponseResource response = new NoContentResponseResource(maybeStatusTransitionFailure.get());
            return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Link link = linkTo(methodOn(ExamController.class).getExamById(examId)).withSelfRel();
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Location", link.getHref());

        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }
}