package tds.exam.web.endpoints;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.HtmlUtils;

import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamineeNote;
import tds.exam.services.ExamineeNoteService;
import tds.exam.web.annotations.VerifyAccess;

@RestController
@RequestMapping("/exam")
public class ExamineeNoteController {
    private final ExamineeNoteService examineeNoteService;

    @Autowired
    public ExamineeNoteController(final ExamineeNoteService examineeNoteService) {
        this.examineeNoteService = examineeNoteService;
    }

    @RequestMapping(value = "/{id}/note", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    @VerifyAccess
    ResponseEntity<ExamineeNote> getNoteInExamContext(@PathVariable final UUID id) {
        Optional<ExamineeNote> maybeExamineeNote = examineeNoteService.findNoteInExamContext(id);

        // It is possible that there is no note for an exam.  When that is the case, return a NO CONTENT response,
        // indicating the call was successful but there was nothing to get.
        return maybeExamineeNote.isPresent()
            ? ResponseEntity.ok(maybeExamineeNote.get())
            : new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @RequestMapping(value = "/{id}/note", method = RequestMethod.PUT, produces = MediaType.APPLICATION_JSON_VALUE)
    @VerifyAccess
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void insert(@PathVariable final UUID id,
                @RequestBody final ExamineeNote note) {
        final ExamineeNote examineeNote = new ExamineeNote.Builder()
            .fromExamineeNote(note)
            .withExamId(id)
            .withNote(HtmlUtils.htmlEscape(note.getNote()))
            .build();

        examineeNoteService.insert(examineeNote);
    }
}
