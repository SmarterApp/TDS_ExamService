package tds.exam.web.endpoints;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

import tds.common.web.exceptions.NotFoundException;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamParameters;
import tds.exam.services.ExpandableExamService;

@RestController
@RequestMapping("/exam")
public class ExpandableExamController {
    private final ExpandableExamService expandableExamService;

    public ExpandableExamController(final ExpandableExamService expandableExamService) {
        this.expandableExamService = expandableExamService;
    }

    @RequestMapping(value = "/session/{sessionId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<ExpandableExam>> findExamsForSessionId(@PathVariable final UUID sessionId,
                                                               @RequestParam(required = false) final Set<String> statusNot,
                                                               @RequestParam(required = false) final ExpandableExamParameters... embed) {
        final List<ExpandableExam> exams = expandableExamService.findExamsBySessionId(sessionId, statusNot, embed);

        return ResponseEntity.ok(exams);
    }

    @RequestMapping(value = "/{examId}/expandable", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ExpandableExam> findExpandableExam(@PathVariable final UUID examId,
                                                      @RequestParam(required = false) final ExpandableExamParameters... embed) {
        ExpandableExam expandableExam = expandableExamService.findExam(examId, embed)
            .orElseThrow(() -> new NotFoundException("Could not find exam for %s", examId));

        return ResponseEntity.ok(expandableExam);
    }
}
