/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

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

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.common.web.exceptions.NotFoundException;
import tds.common.web.resources.NoContentResponseResource;
import tds.exam.ApproveAccommodationsRequest;
import tds.exam.Exam;
import tds.exam.ExamAssessmentMetadata;
import tds.exam.ExamConfiguration;
import tds.exam.ExamStatusCode;
import tds.exam.ExamStatusRequest;
import tds.exam.OpenExamRequest;
import tds.exam.SegmentApprovalRequest;
import tds.exam.services.ExamService;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static tds.exam.ExamStatusStage.INACTIVE;

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
    ResponseEntity<Response<ExamConfiguration>> startExam(@PathVariable final UUID examId, @RequestBody final String browserUserAgent) {
        Response<ExamConfiguration> examConfiguration = examService.startExam(examId, browserUserAgent);

        if (examConfiguration.hasError()) {
            return new ResponseEntity<>(examConfiguration, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ResponseEntity.ok(examConfiguration);
    }

    @RequestMapping(value = "/{examId}/status", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<NoContentResponseResource> updateStatus(@PathVariable final UUID examId,
                                                           @RequestBody final ExamStatusRequest examStatusRequest) {
        final Optional<ValidationError> maybeStatusTransitionFailure = examService.updateExamStatus(examId,
            examStatusRequest.getExamStatus(), examStatusRequest.getReason());

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
            new ExamStatusCode(ExamStatusCode.STATUS_PAUSED, INACTIVE));

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
    void pauseExamsInSession(@PathVariable final UUID sessionId) {
        examService.pauseAllExamsInSession(sessionId);
    }

    @RequestMapping(value = "/{examId}/segmentApproval", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<NoContentResponseResource> waitForSegmentApproval(@PathVariable final UUID examId,
                                                                     @RequestBody final SegmentApprovalRequest request) {
        final Optional<ValidationError> maybeStatusTransitionFailure = examService.waitForSegmentApproval(examId, request);

        if (maybeStatusTransitionFailure.isPresent()) {
            NoContentResponseResource response = new NoContentResponseResource(maybeStatusTransitionFailure.get());
            return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Link link = linkTo(methodOn(ExamController.class).getExamById(examId)).withSelfRel();
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Location", link.getHref());

        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/{examId}/accommodations", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<NoContentResponseResource> approveAccommodations(@PathVariable final UUID examId, @RequestBody ApproveAccommodationsRequest request) {
        Optional<ValidationError> maybeError = examService.updateExamAccommodationsAndExam(examId, request);

        if (maybeError.isPresent()) {
            NoContentResponseResource response = new NoContentResponseResource(maybeError.get());
            return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        Link link = linkTo(methodOn(ExamAccommodationController.class).findAccommodations(examId)).withSelfRel();
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Location", link.getHref());

        return new ResponseEntity<>(headers, HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/metadata", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Response<List<ExamAssessmentMetadata>>> findExamAssessmentMetadata(@RequestParam final long studentId,
                                                                            @RequestParam final UUID sessionId,
                                                                            @RequestParam final String grade) {
        Response<List<ExamAssessmentMetadata>> response = examService.findExamAssessmentMetadata(studentId, sessionId, grade);

        if (response.getError().isPresent()) {
            return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ResponseEntity.ok(response);
    }
}