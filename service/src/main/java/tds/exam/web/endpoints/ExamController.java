package tds.exam.web.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import tds.common.Response;
import tds.common.web.exceptions.NotFoundException;
import tds.exam.Exam;
import tds.exam.OpenExam;
import tds.exam.services.ExamService;
import tds.exam.web.resources.ExamResource;

import java.util.UUID;

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
    public ResponseEntity<Void> openExam(@RequestBody final OpenExam openExam) {
        Response<Exam> examOptional = examService.openExam(openExam);

        if (!examOptional.getData().isPresent()) {
            //TODO - Determine best practice
            return ResponseEntity.badRequest().build();
        }

        ExamResource resource = new ExamResource(examOptional.getData().get());
        final HttpHeaders headers = new HttpHeaders();
        headers.add("Location", resource.getLink("self").getHref());

        return new ResponseEntity<>(null, headers, HttpStatus.CREATED);
    }
}

