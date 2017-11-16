package tds.exam.services.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import tds.exam.ExpireExamsResult;
import tds.exam.repositories.ExamQueryRepository;
import tds.exam.services.ExamExpirationService;
import tds.exam.services.ExamService;

public class ExamExpirationServiceImpl implements ExamExpirationService {
    private static final Logger LOG = LoggerFactory.getLogger(ExamExpirationServiceImpl.class);
    private final ExamService examService;
    private final ExamQueryRepository examQueryRepository;

    public ExamExpirationServiceImpl(final ExamService examService, final ExamQueryRepository examQueryRepository) {
        this.examService = examService;
        this.examQueryRepository = examQueryRepository;
    }

    @Override
    public ExpireExamsResult expireExams() {
        return null;
    }
}
