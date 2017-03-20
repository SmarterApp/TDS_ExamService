package tds.exam.web.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.common.ValidationError;
import tds.common.web.resources.NoContentResponseResource;
import tds.exam.ExamSegment;
import tds.exam.services.ExamSegmentService;
import tds.exam.web.annotations.VerifyAccess;

@RestController
@RequestMapping("/exam")
public class ExamSegmentController {
    private final ExamSegmentService examSegmentService;

    @Autowired
    public ExamSegmentController(final ExamSegmentService examSegmentService) {
        this.examSegmentService = examSegmentService;
    }

    @VerifyAccess
    @RequestMapping(value = "/{examId}/segments", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<ExamSegment>> getExamSegments(@PathVariable final UUID examId) {
        return ResponseEntity.ok(examSegmentService.findExamSegments(examId));
    }

    @RequestMapping(value ="/{examId}/segments/completed", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Boolean> checkIfSegmentsCompleted(@PathVariable final UUID examId) {
        return ResponseEntity.ok(examSegmentService.checkIfSegmentsCompleted(examId));
    }

    @RequestMapping(value = "/{examId}/segments/{segmentPosition}/exit", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<NoContentResponseResource> exitSegment(@PathVariable final UUID examId,
                                                          @PathVariable final int segmentPosition) {
        Optional<ValidationError> maybeError = examSegmentService.exitSegment(examId, segmentPosition);

        if (maybeError.isPresent()) {
            NoContentResponseResource response = new NoContentResponseResource(maybeError.get());
            return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
