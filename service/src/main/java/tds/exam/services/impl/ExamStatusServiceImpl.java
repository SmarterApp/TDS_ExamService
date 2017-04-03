package tds.exam.services.impl;

import com.google.common.base.Optional;
import org.joda.time.Instant;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

import tds.exam.repositories.ExamStatusQueryRepository;
import tds.exam.services.ExamStatusService;

@Service
public class ExamStatusServiceImpl implements ExamStatusService {
    private final ExamStatusQueryRepository examStatusQueryRepository;

    @Autowired
    public ExamStatusServiceImpl(final ExamStatusQueryRepository examStatusQueryRepository) {
        this.examStatusQueryRepository = examStatusQueryRepository;
    }

    @Override
    public Optional<Instant> findDateLastTimeStatus(final UUID examId, final String examStatus) {
        return examStatusQueryRepository.findDateLastTimeStatus(examId, examStatus);
    }
}
