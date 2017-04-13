package tds.exam.web.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
import tds.exam.ExamStatusCode;
import tds.exam.services.ExamItemService;
import tds.exam.web.annotations.VerifyAccess;

import static org.springframework.hateoas.mvc.ControllerLinkBuilder.linkTo;
import static org.springframework.hateoas.mvc.ControllerLinkBuilder.methodOn;
import static tds.exam.ExamStatusStage.INACTIVE;

@RestController
@RequestMapping("/exam")
public class ExamItemController {
    private final ExamItemService examItemService;

    @Autowired
    public ExamItemController(ExamItemService examItemService) {
        this.examItemService = examItemService;
    }

    @PostMapping("/{id}/page/{position}/responses")
    @VerifyAccess
    ResponseEntity<Response<ExamPage>> insertResponses(@PathVariable final UUID id,
                                                       @PathVariable final int position,
                                                       @RequestBody final ExamItemResponse[] responses) {
        Response<ExamPage> nextPage = examItemService.insertResponses(id,
            position,
            responses);

        if (nextPage.hasError()) {
            return new ResponseEntity<>(nextPage, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return ResponseEntity.ok(nextPage);
    }

    @PostMapping("/{id}/item/{position}/review")
    ResponseEntity<NoContentResponseResource> markItemForReview(@PathVariable final UUID id,
                                                                @PathVariable final int position,
                                                                @RequestBody final Boolean mark) {
        final Optional<ValidationError> maybeMarkFailure = examItemService.markForReview(id, position, mark);

        if (maybeMarkFailure.isPresent()) {
            NoContentResponseResource response = new NoContentResponseResource(maybeMarkFailure.get());
            return new ResponseEntity<>(response, HttpStatus.UNPROCESSABLE_ENTITY);
        }

        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
