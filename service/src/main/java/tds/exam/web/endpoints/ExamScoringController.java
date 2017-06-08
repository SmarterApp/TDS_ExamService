package tds.exam.web.endpoints;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import tds.exam.web.annotations.VerifyAccess;
import tds.score.model.ExamInstance;
import tds.score.services.ItemScoringService;
import tds.student.sql.data.ItemResponseUpdate;
import tds.student.sql.data.ItemResponseUpdateStatus;

import java.util.List;
import java.util.UUID;

/**
 * This controller is responsible for providing scoring information for exams.
 */
@RestController
@RequestMapping("/exam/{examId}/scores")
public class ExamScoringController {

    private final ItemScoringService itemScoringService;

    @Autowired
    ExamScoringController(final ItemScoringService itemScoringService) {
        this.itemScoringService = itemScoringService;
    }

    @RequestMapping(value = "/responses", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @VerifyAccess
    public ResponseEntity<List<ItemResponseUpdateStatus>> updateResponses(@PathVariable final UUID examId,
                                                                          @RequestParam final UUID sessionId,
                                                                          @RequestParam final UUID browserId,
                                                                          @RequestParam final String clientName,
                                                                          @RequestParam final Float pageDuration,
                                                                          @RequestBody final List<ItemResponseUpdate> responseUpdates) throws ReturnStatusException {
        final ExamInstance examInstance = ExamInstance.create(examId, sessionId, browserId, clientName);
        final List<ItemResponseUpdateStatus> responses = itemScoringService.updateResponses(examInstance, responseUpdates, pageDuration);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }
}
