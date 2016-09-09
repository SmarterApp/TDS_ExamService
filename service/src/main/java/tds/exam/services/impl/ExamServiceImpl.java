package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tds.exam.Exam;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.services.ExamService;

import java.util.Optional;
import java.util.UUID;

@Service
public class ExamServiceImpl implements ExamService {
    private final ExamQueryRepository examQueryRepository;

    @Autowired
    public ExamServiceImpl(ExamQueryRepository examQueryRepository) {
        this.examQueryRepository = examQueryRepository;
    }

    @Override
    public Optional<Exam> getExam(UUID id) {
        return examQueryRepository.getExamById(id);
    }
}
