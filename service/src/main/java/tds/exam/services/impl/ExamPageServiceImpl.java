package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import tds.exam.ExamPage;
import tds.exam.repositories.ExamPageCommandRepository;
import tds.exam.repositories.ExamPageQueryRepository;
import tds.exam.services.ExamPageService;

@Service
public class ExamPageServiceImpl implements ExamPageService {
    private final ExamPageCommandRepository examPageCommandRepository;
    private final ExamPageQueryRepository examPageQueryRepository;


    @Autowired
    public ExamPageServiceImpl(final ExamPageQueryRepository examPageQueryRepository,
                               final ExamPageCommandRepository examPageCommandRepository) {
        this.examPageCommandRepository = examPageCommandRepository;
        this.examPageQueryRepository = examPageQueryRepository;
    }

    @Transactional
    @Override
    public void insertPages(final ExamPage... examPages) {
        examPageCommandRepository.insert(examPages);
    }

    @Transactional
    @Override
    public void deletePages(final UUID examId) {
        examPageCommandRepository.deleteAll(examId);
    }

    @Override
    public List<ExamPage> findAllPages(final UUID examId) {
        return examPageQueryRepository.findAll(examId);
    }

    @Override
    public Optional<ExamPage> find(final UUID id) {
        return examPageQueryRepository.find(id);
    }

    @Override
    public void update(final ExamPage... examPages) {
        examPageCommandRepository.update(examPages);
    }
}
