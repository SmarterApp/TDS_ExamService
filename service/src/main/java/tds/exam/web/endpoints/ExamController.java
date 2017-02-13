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
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.common.web.exceptions.NotFoundException;
import tds.common.web.resources.NoContentResponseResource;
import tds.exam.Exam;
import tds.exam.ExamConfiguration;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusStage;
import tds.exam.OpenExamRequest;
import tds.exam.services.ExamService;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static tds.exam.ExamStatusCode.STATUS_APPROVED;
import static tds.exam.ExamStatusCode.STATUS_DENIED;
import static tds.exam.ExamStatusStage.OPEN;

@RestController
@RequestMapping("/exam")
public class ExamController {
    private final ExamService examService;

    public static final ExamStatusCode APPROVED_STATUS = new ExamStatusCode(STATUS_APPROVED, OPEN);
    public static final ExamStatusCode DENIED_STATUS = new ExamStatusCode(STATUS_DENIED, OPEN);

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

        if (exam.hasError()) {
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

        if (examConfiguration.hasError()) {
            return new ResponseEntity<>(examConfiguration, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ResponseEntity.ok(examConfiguration);
    }
    
    @RequestMapping(value = "/{examId}/status", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<NoContentResponseResource> updateStatus(@PathVariable final UUID examId,
                                                           @RequestParam final String status,
                                                           @RequestParam(required = false) final String stage,
                                                           @RequestParam(required = false) final String reason) {
    
        ExamStatusCode examStatus = (stage == null) ? new ExamStatusCode(status) : new ExamStatusCode(status, ExamStatusStage.fromType(stage));
        final Optional<ValidationError> maybeStatusTransitionFailure = examService.updateExamStatus(examId, examStatus, reason);
    
        if (maybeStatusTransitionFailure.isPresent()) {
            NoContentResponseResource response = new NoContentResponseResource(maybeStatusTransitionFailure.get());
            return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
        }
    
        Link link = linkTo(methodOn(ExamController.class).getExamById(examId)).withSelfRel();
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Location", link.getHref());
    
        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/{examId}/pause", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<NoContentResponseResource> pauseExam(@PathVariable final UUID examId) {
        final Optional<ValidationError> maybeStatusTransitionFailure = examService.updateExamStatus(examId,
            new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, ExamStatusStage.INACTIVE));

        if (maybeStatusTransitionFailure.isPresent()) {
            NoContentResponseResource response = new NoContentResponseResource(maybeStatusTransitionFailure.get());
            return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Link link = linkTo(methodOn(ExamController.class).getExamById(examId)).withSelfRel();
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Location", link.getHref());

        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/pause/{sessionId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void pauseExamsInSession(@PathVariable final UUID sessionId) {
        examService.pauseAllExamsInSession(sessionId);
    }

    @RequestMapping(value = "/approve/{examId}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<NoContentResponseResource> approveExam(@PathVariable final UUID examId) {
        return updateExamStatus(examId, APPROVED_STATUS, Optional.empty());
    }

    @RequestMapping(value = "/deny/{examId}", method = RequestMethod.PUT, consumes = MediaType.TEXT_PLAIN_VALUE,  produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<NoContentResponseResource> denyExam(@PathVariable final UUID examId, @RequestBody final String reason) {
        return updateExamStatus(examId, DENIED_STATUS, Optional.of(reason));
    }

    private ResponseEntity<NoContentResponseResource> updateExamStatus(UUID examId, ExamStatusCode examStatusCode, Optional<String> reason) {
        final Optional<ValidationError> maybeStatusTransitionFailure = reason.isPresent() ?
            examService.updateExamStatus(examId, examStatusCode, reason.get()) :
            examService.updateExamStatus(examId, examStatusCode);

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