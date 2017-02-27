package tds.exam.web.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import tds.exam.ExamPrintRequest;
import tds.exam.services.ExamPrintRequestService;

@RestController
@RequestMapping("/exam/print")
public class ExamPrintRequestController {
    private final ExamPrintRequestService examPrintRequestService;

    @Autowired
    public ExamPrintRequestController(final ExamPrintRequestService examPrintRequestService) {
        this.examPrintRequestService = examPrintRequestService;
    }

    @RequestMapping(method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void createExamRequest(@RequestBody ExamPrintRequest request) {
        examPrintRequestService.insert(request);
    }
}
