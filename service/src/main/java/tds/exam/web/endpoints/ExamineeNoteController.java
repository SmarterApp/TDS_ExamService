/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

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
        // Only Item-level notes are HTML-escaped in legacy student application (TestShellHandler#RecordItemComment,
        // line 413).  The exam database will store the notes in a consistent manner; the caller can format the note
        // text however they see fit.
        final ExamineeNote examineeNote = ExamineeNote.Builder.fromExamineeNote(note)
            .withExamId(id)
            .withNote(HtmlUtils.htmlUnescape(note.getNote()))
            .build();

        examineeNoteService.insert(examineeNote);
    }
}
