package tds.exam.services.impl;

import org.springframework.stereotype.Service;

import tds.exam.ExamItemResponse;
import tds.exam.ExamItemResponseScore;
import tds.exam.ExamScoringStatus;
import tds.exam.services.ExamItemResponseScoringService;

@Service
public class TemporaryExamItemResponseScoringServiceImpl implements ExamItemResponseScoringService {
    @Override
    public ExamItemResponseScore getScore(final ExamItemResponse response) {
        // This implementation is temporary until scoring logic is ported over from legacy.  in particular, this
        // return value emulates returning an "empty"/dummy score when isScoringAsynchronous() == true
        // (tds.student.services.ItemScoringService, line 286).
        return new ExamItemResponseScore.Builder()
            .withScore(-1)
            .withScoringStatus(ExamScoringStatus.WAITING_FOR_MACHINE_SCORE)
            .withScoringRationale("Waiting for machine score")
            .build();
    }
}
