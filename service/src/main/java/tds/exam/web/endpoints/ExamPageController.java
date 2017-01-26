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

import java.util.UUID;

import tds.common.Response;
import tds.exam.ApprovalRequest;
import tds.exam.ExamPage;
import tds.exam.services.ExamPageService;

@RestController
@RequestMapping("/exam")
public class ExamPageController {
    private final ExamPageService examPageService;

    @Autowired
    public ExamPageController(ExamPageService examPageService) {
        this.examPageService = examPageService;
    }

    @RequestMapping(value = "/{id}/page/{position}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<Response<ExamPage>> getPage(@PathVariable final UUID id,
                                               @PathVariable final int position,
                                               @RequestParam final UUID sessionId,
                                               @RequestParam final UUID browserId,
                                               @RequestParam final String clientName) {
        ApprovalRequest approvalRequest = new ApprovalRequest(id, sessionId, browserId, clientName);
        Response<ExamPage> page = examPageService.getPage(approvalRequest, position);

        if (page.hasError()) {
            return new ResponseEntity<>(page, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ResponseEntity.ok(page);
    }
}
