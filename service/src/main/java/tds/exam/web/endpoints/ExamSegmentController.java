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
}
