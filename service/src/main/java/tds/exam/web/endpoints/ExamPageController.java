package tds.exam.web.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

import tds.exam.ExamPage;
import tds.exam.services.ExamPageService;
import tds.exam.web.annotations.VerifyAccess;

@RestController
@RequestMapping("/exam")
public class ExamPageController {
    private final ExamPageService examPageService;

    @Autowired
    public ExamPageController(ExamPageService examPageService) {
        this.examPageService = examPageService;
    }

    @VerifyAccess
    @RequestMapping(value = "/{id}/page/{position}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<ExamPage> getPage(@PathVariable final UUID id,
                                     @PathVariable final int position) {
        return ResponseEntity.ok(examPageService.getPage(id, position));
    }

    @VerifyAccess
    @RequestMapping(value = "/{id}/page", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    ResponseEntity<List<ExamPage>> getAllPagesWithItems(@PathVariable final UUID id) {
        List<ExamPage> examPagesWithItems = examPageService.findAllPagesWithItems(id);

        return ResponseEntity.ok(examPagesWithItems);
    }
}
