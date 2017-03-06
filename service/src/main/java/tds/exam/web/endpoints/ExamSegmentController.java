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
import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.common.web.resources.NoContentResponseResource;
import tds.exam.ExamSegment;
import tds.exam.services.ExamSegmentService;

@RestController
@RequestMapping("/exam/segments")
public class ExamSegmentController {
    private final ExamSegmentService examSegmentService;

    @Autowired
    public ExamSegmentController(final ExamSegmentService examSegmentService) {
        this.examSegmentService = examSegmentService;
    }

    @RequestMapping(value = "/{examId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Response<List<ExamSegment>>> getExamSegments(@PathVariable final UUID examId,
                                                                @RequestParam final UUID browserId,
                                                                @RequestParam final UUID sessionId) {
        Response<List<ExamSegment>> examSegmentsResponse = examSegmentService.findExamSegments(examId, sessionId, browserId);

        if (examSegmentsResponse.getError().isPresent()) {
            return new ResponseEntity<>(examSegmentsResponse, HttpStatus.UNPROCESSABLE_ENTITY);
        } else if (examSegmentsResponse.getData().isPresent() && examSegmentsResponse.getData().get().isEmpty()) {
            return new ResponseEntity<>(examSegmentsResponse, HttpStatus.NO_CONTENT);
        }

        return ResponseEntity.ok(examSegmentsResponse);
    }

    @RequestMapping(value = "/{examId}/exit/{segmentPosition}", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
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
