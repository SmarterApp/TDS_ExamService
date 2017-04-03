package tds.exam.services.mappers.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import tds.exam.ExamineeNote;
import tds.exam.ExpandableExam;
import tds.exam.ExpandableExamParameters;
import tds.exam.services.ExamineeNoteService;
import tds.exam.services.mappers.ExpandableExamMapper;

@Component
public class ExamineeNotesExpandableExamMapper implements ExpandableExamMapper {
    private final ExamineeNoteService examineeNoteService;

    @Autowired
    public ExamineeNotesExpandableExamMapper(final ExamineeNoteService examineeNoteService) {
        this.examineeNoteService = examineeNoteService;
    }

    @Override
    public void updateExpandableMapper(final Set<ExpandableExamParameters> expandableExamAttributes, final Map<UUID, ExpandableExam.Builder> examBuilders, final UUID sessionId) {
        if (!expandableExamAttributes.contains(ExpandableExamParameters.EXPANDABLE_PARAMS_EXAM_NOTES)) {
            return;
        }

        examBuilders.forEach((examId, examBuilder) -> {
            List<ExamineeNote> examineeNotes = examineeNoteService.findAllNotes(examId);
            ExpandableExam.Builder builder = examBuilders.get(examId);
            builder.withExamineeNotes(examineeNotes);
        });
    }
}
