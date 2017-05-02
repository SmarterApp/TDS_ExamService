package tds.exam.web.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.common.ValidationError;
import tds.common.web.exceptions.NotFoundException;
import tds.common.web.resources.NoContentResponseResource;
import tds.exam.ExamSegment;
import tds.exam.services.ExamSegmentService;
import tds.exam.web.annotations.VerifyAccess;
import tds.exam.wrapper.ExamSegmentWrapper;

@RestController
@RequestMapping("/exam")
public class ExamSegmentController {
    private final ExamSegmentService examSegmentService;

    @Autowired
    public ExamSegmentController(final ExamSegmentService examSegmentService) {
        this.examSegmentService = examSegmentService;
    }

    @VerifyAccess
    @GetMapping(value = "/{examId}/segments", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<ExamSegment>> getExamSegments(@PathVariable final UUID examId) {
        return ResponseEntity.ok(examSegmentService.findExamSegments(examId));
    }

    @GetMapping(value = "/{examId}/segments/completed", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Boolean> checkIfSegmentsCompleted(@PathVariable final UUID examId) {
        return ResponseEntity.ok(examSegmentService.checkIfSegmentsCompleted(examId));
    }

    @PutMapping(value = "/{examId}/segments/{segmentPosition}/exit", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<NoContentResponseResource> exitSegment(@PathVariable final UUID examId,
                                                          @PathVariable final int segmentPosition) {
        Optional<ValidationError> maybeError = examSegmentService.exitSegment(examId, segmentPosition);

        if (maybeError.isPresent()) {
            NoContentResponseResource response = new NoContentResponseResource(maybeError.get());
            return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "{examId}/segmentWrappers", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<ExamSegmentWrapper>> findExamSegmentWrappersForExam(@PathVariable final UUID examId) {
        List<ExamSegmentWrapper> wrappers = examSegmentService.findAllExamSegments(examId);

        if (wrappers.isEmpty()) {
            throw new NotFoundException("Could not find exam segments for exam %s", examId);
        }

        return ResponseEntity.ok(wrappers);
    }

    @GetMapping(value = "{examId}/segmentWrappers/{segmentPosition}", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ExamSegmentWrapper> findExamSegmentWrapperForExamAndSegmentPosition(@PathVariable final UUID examId,
                                                                                       @PathVariable final int segmentPosition,
                                                                                       @RequestParam(value = "pagePosition", required = false) Integer pagePosition) {
        if (pagePosition == null) {
            ExamSegmentWrapper examSegmentWrapper = examSegmentService.findExamSegment(examId, segmentPosition)
                .orElseThrow(() -> new NotFoundException("Could not find exam segment for exam %s and position %d", examId, segmentPosition));

            return ResponseEntity.ok(examSegmentWrapper);
        }

        ExamSegmentWrapper examSegmentWrapper = examSegmentService.findExamSegmentWithPageAtPosition(examId, segmentPosition, pagePosition)
            .orElseThrow(() -> new NotFoundException("Could not find exam segment for exam %s, segment position %d, and page position %d", examId, segmentPosition, pagePosition));

        return ResponseEntity.ok(examSegmentWrapper);
    }
}
