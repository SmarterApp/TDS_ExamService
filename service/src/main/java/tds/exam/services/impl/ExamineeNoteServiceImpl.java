package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamineeNote;
import tds.exam.repositories.ExamineeNoteCommandRepository;
import tds.exam.repositories.ExamineeNoteQueryRepository;
import tds.exam.services.ExamineeNoteService;

@Service
public class ExamineeNoteServiceImpl implements ExamineeNoteService {
    private final ExamineeNoteCommandRepository examineeNoteCommandRepository;
    private final ExamineeNoteQueryRepository examineeNoteQueryRepository;

    @Autowired
    public ExamineeNoteServiceImpl(final ExamineeNoteCommandRepository examineeNoteCommandRepository,
                                   final ExamineeNoteQueryRepository examineeNoteQueryRepository) {
        this.examineeNoteCommandRepository = examineeNoteCommandRepository;
        this.examineeNoteQueryRepository = examineeNoteQueryRepository;
    }

    @Override
    public Optional<ExamineeNote> findNoteInExamContext(final UUID examId) {
        return examineeNoteQueryRepository.findNoteInExamContext(examId);
    }

    @Override
    public List<ExamineeNote> findAllNotes(final UUID examId) {
        return examineeNoteQueryRepository.findAllNotes(examId);
    }

    @Override
    public void insert(final ExamineeNote examineeNote) {
        examineeNoteCommandRepository.insert(examineeNote);
    }
}
