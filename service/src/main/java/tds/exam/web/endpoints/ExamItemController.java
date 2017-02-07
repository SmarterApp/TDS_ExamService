package tds.exam.web.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

import tds.common.Response;
import tds.exam.ApprovalRequest;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.exam.services.ExamItemService;

@RestController
@RequestMapping("/exam")
public class ExamItemController {
    private final ExamItemService examItemService;

    @Autowired
    public ExamItemController(ExamItemService examItemService) {
        this.examItemService = examItemService;
    }

    //@RequestMapping(value = "/{id}/page/{position}/responses", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @PostMapping("/{id}/page/{position}/responses")
    ResponseEntity<Response<ExamPage>> insertResponses(@PathVariable final UUID id,
                                                       @PathVariable final int position,
                                                       @RequestParam final UUID sessionId,
                                                       @RequestParam final UUID browserId,
                                                       @RequestBody final ExamItemResponse[] responses) {
        ApprovalRequest approvalRequest = new ApprovalRequest(id, sessionId, browserId);
        Response<ExamPage> nextPage = examItemService.insertResponses(approvalRequest,
            position,
            responses);

        if (nextPage.hasError()) {
            return new ResponseEntity<>(nextPage, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ResponseEntity.ok(nextPage);
    }
}
