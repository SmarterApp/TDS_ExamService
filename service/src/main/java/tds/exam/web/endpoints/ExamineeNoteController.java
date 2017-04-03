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
import tds.exam.ExamineeNoteContext;
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

        // It is possible that there is no note for an exam.  When that is the case, return a NOT FOUND response - the
        // caller can decide if that's an error condition
        return maybeExamineeNote.isPresent()
            ? ResponseEntity.ok(maybeExamineeNote.get())
            : new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @RequestMapping(value = "/{id}/note", method = RequestMethod.POST, produces = MediaType.APPLICATION_JSON_VALUE)
    @VerifyAccess
    @ResponseStatus(HttpStatus.NO_CONTENT)
    void insert(@PathVariable final UUID id,
                @RequestBody final ExamineeNote note) {
        // In legacy the student application, only item-level notes are HTML-escaped and exam-level notes are not
        // (TestShellHandler#RecordItemComment, line 413 and TestShellHandler#RecordOppComment, line 436).  Since the
        // note text is already escaped by the time it gets here, it must be un-escaped for Exam-level notes.
        final String noteText = note.getContext().equals(ExamineeNoteContext.EXAM)
            ? HtmlUtils.htmlUnescape(note.getNote())
            : note.getNote();

        final ExamineeNote examineeNote = ExamineeNote.Builder.fromExamineeNote(note)
            .withExamId(id)
            .withNote(noteText)
            .build();

        examineeNoteService.insert(examineeNote);
    }
}
