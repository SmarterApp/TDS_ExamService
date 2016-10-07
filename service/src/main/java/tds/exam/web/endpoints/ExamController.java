package tds.exam.web.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.common.web.exceptions.NotFoundException;
import tds.exam.Exam;
import tds.exam.ExamApproval;
import tds.exam.ExamApprovalRequest;
import tds.exam.OpenExamRequest;
import tds.exam.services.ExamService;
import tds.exam.web.resources.ExamApprovalResource;
import tds.exam.web.resources.ExamResource;

import javax.xml.ws.RequestWrapper;

@RestController
@RequestMapping("/exam")
public class ExamController {
    private final ExamService examService;

    @Autowired
    public ExamController(ExamService examService) {
        this.examService = examService;
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
        ExamApprovalRequest examApprovalRequest = new ExamApprovalRequest(examId, sessionId, browserId, clientName);
        Response<ExamApproval> examApproval = examService.getApproval(examApprovalRequest);

        if (examApproval.getErrors().isPresent()) {
            return new ResponseEntity<>(new ExamApprovalResource(examApproval), HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ResponseEntity.ok(new ExamApprovalResource(examApproval));
    }
}

