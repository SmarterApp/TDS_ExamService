package tds.exam.web.endpoints;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.MatrixVariable;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import tds.common.Response;
import tds.common.web.exceptions.NotFoundException;
import tds.exam.Accommodation;
import tds.exam.ApprovalRequest;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.OpenExamRequest;
import tds.exam.services.AccommodationService;
import tds.exam.services.ExamService;
import tds.exam.web.resources.ExamApprovalResource;
import tds.exam.web.resources.ExamResource;

@RestController
@RequestMapping("/exam")
public class ExamController {
    private final ExamService examService;
    private final AccommodationService accommodationService;

    @Autowired
    public ExamController(ExamService examService, AccommodationService accommodationService) {
        this.examService = examService;
        this.accommodationService = accommodationService;
    }

    @RequestMapping(value = "/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExamResource> getExamById(@PathVariable UUID id) {
        final Exam exam = examService.getExam(id)
            .orElseThrow(() -> new NotFoundException("Could not find exam for %s", id));

        return ResponseEntity.ok(new ExamResource(exam));
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExamResource> openExam(@RequestBody final OpenExamRequest openExamRequest) {
        Response<Exam> exam = examService.openExam(openExamRequest);

        ExamResource resource = new ExamResource(exam);

        if(!exam.getData().isPresent()) {
            return new ResponseEntity<>(new ExamResource(exam), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        final HttpHeaders headers = new HttpHeaders();
        headers.add("Location", resource.getLink("self").getHref());
        return new ResponseEntity<>(resource, headers, HttpStatus.OK);
    }

    @RequestMapping(value = "/{id}/get-approval", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ExamApprovalResource> getApproval(@PathVariable final UUID examId, @RequestParam final UUID sessionId, @RequestParam final UUID browserId, final String clientName) {
        ApprovalRequest approvalRequest = new ApprovalRequest(examId, sessionId, browserId, clientName);
        Response<ExamApproval> examApproval = examService.getApproval(approvalRequest);

        if (examApproval.getErrors().isPresent()) {
            return new ResponseEntity<>(new ExamApprovalResource(examApproval), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ResponseEntity.ok(new ExamApprovalResource(examApproval));
    }

    @RequestMapping(value = "/{id}/{segmentId}/accommodations/{accommodationTypes}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<List<Accommodation>> getAccommodations(@PathVariable final UUID id,
                                                                 @PathVariable final String segmentId,
                                                                 @MatrixVariable(required = false) final String[] accommodationTypes) {
        if (accommodationTypes == null || accommodationTypes.length == 0) {
            throw new IllegalArgumentException("accommodation types with values are required");
        }

        return ResponseEntity.ok(accommodationService.findAccommodations(id, segmentId, accommodationTypes));
    }
}

