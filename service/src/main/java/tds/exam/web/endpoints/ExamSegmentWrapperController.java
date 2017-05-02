package tds.exam.web.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.common.web.exceptions.NotFoundException;
import tds.exam.services.ExamSegmentWrapperService;
import tds.exam.wrapper.ExamSegmentWrapper;

@RestController
@RequestMapping("/exam")
public class ExamSegmentWrapperController {
    private final ExamSegmentWrapperService examSegmentWrapperService;

    @Autowired
    public ExamSegmentWrapperController(final ExamSegmentWrapperService examSegmentWrapperService) {
        this.examSegmentWrapperService = examSegmentWrapperService;
    }

    @GetMapping(value = "{examId}/segmentWrappers", produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<ExamSegmentWrapper>> findExamSegmentWrappersForExam(@PathVariable final UUID examId,
                                                                            @RequestParam(value = "pagePosition", required = false) Integer pagePosition) {
        List<ExamSegmentWrapper> wrappers = Collections.emptyList();

        if (pagePosition != null) {
            Optional<ExamSegmentWrapper> maybeWrapper = examSegmentWrapperService.findExamSegmentWithPageAtPosition(examId, pagePosition);
            if (maybeWrapper.isPresent()) {
                wrappers = Collections.singletonList(maybeWrapper.get());
            }
        } else {
            wrappers = examSegmentWrapperService.findAllExamSegments(examId);
        }

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
            ExamSegmentWrapper examSegmentWrapper = examSegmentWrapperService.findExamSegment(examId, segmentPosition)
                .orElseThrow(() -> new NotFoundException("Could not find exam segment for exam %s and position %d", examId, segmentPosition));

            return ResponseEntity.ok(examSegmentWrapper);
        }

        ExamSegmentWrapper examSegmentWrapper = examSegmentWrapperService.findExamSegmentWithPageAtPosition(examId, segmentPosition, pagePosition)
            .orElseThrow(() -> new NotFoundException("Could not find exam segment for exam %s, segment position %d, and page position %d", examId, segmentPosition, pagePosition));

        return ResponseEntity.ok(examSegmentWrapper);
    }
}
