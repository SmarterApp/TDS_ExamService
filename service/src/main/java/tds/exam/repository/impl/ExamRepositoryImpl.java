package tds.exam.repository.impl;

import org.springframework.stereotype.Repository;
import tds.exam.Exam;
import tds.exam.repository.ExamRepository;

import java.util.UUID;

@Repository
public class ExamRepositoryImpl implements ExamRepository {

    @Override
    public Exam getExamById(UUID id) {
        return new Exam(id);
    }
}
