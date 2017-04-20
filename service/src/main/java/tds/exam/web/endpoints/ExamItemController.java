package tds.exam.web.endpoints;

import TDS.Shared.Exceptions.ReturnStatusException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Optional;
import java.util.UUID;

import tds.common.Response;
import tds.common.ValidationError;
import tds.common.web.resources.NoContentResponseResource;
import tds.exam.ExamItemResponse;
import tds.exam.ExamPage;
import tds.exam.item.PageGroupRequest;
import tds.exam.services.ExamItemSelectionService;
import tds.exam.services.ExamItemService;
import tds.exam.web.annotations.VerifyAccess;
import tds.student.services.data.PageGroup;

@RestController
@RequestMapping("/exam")
public class ExamItemController {
    private final ExamItemService examItemService;
    private final ExamItemSelectionService examItemSelectionService;

    @Autowired
    public ExamItemController(final ExamItemService examItemService,
                              final ExamItemSelectionService examItemSelectionService) {
        this.examItemService = examItemService;
        this.examItemSelectionService = examItemSelectionService;
    }

    @PostMapping("/{examId}/page/{position}/responses")
    @VerifyAccess
    ResponseEntity<Response<ExamPage>> insertResponses(@PathVariable final UUID examId,
                                                       @PathVariable final int position,
                                                       @RequestBody final ExamItemResponse[] responses) {
        Response<ExamPage> nextPage = examItemService.insertResponses(examId,
            position,
            responses);

        if (nextPage.hasError()) {
            return new ResponseEntity<>(nextPage, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ResponseEntity.ok(nextPage);
    }

    @PutMapping("/{examId}/item/{position}/review")
    @VerifyAccess
    ResponseEntity<NoContentResponseResource> markItemForReview(@PathVariable final UUID examId,
                                                                @PathVariable final int position,
                                                                @RequestBody final Boolean mark) {
        final Optional<ValidationError> maybeMarkFailure = examItemService.markForReview(examId, position, mark);

        if (maybeMarkFailure.isPresent()) {
            NoContentResponseResource response = new NoContentResponseResource(maybeMarkFailure.get());
            return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @PostMapping("/{examId}/item")
    ResponseEntity<PageGroup> getNextItemGroup(@PathVariable final UUID examId, @RequestBody PageGroupRequest request) throws ReturnStatusException {
        return ResponseEntity.ok(examItemSelectionService.createNextPageGroup(examId, request));
    }
}
