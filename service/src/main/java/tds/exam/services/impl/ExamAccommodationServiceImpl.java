package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import tds.config.Accommodation;
import tds.exam.Exam;
import tds.exam.ExamAccommodation;
import tds.exam.repositories.ExamAccommodationCommandRepository;
import tds.exam.repositories.ExamAccommodationQueryRepository;
import tds.exam.services.ConfigService;
import tds.exam.services.ExamAccommodationService;

@Service
class ExamAccommodationServiceImpl implements ExamAccommodationService {
    private final ExamAccommodationQueryRepository examAccommodationQueryRepository;
    private final ExamAccommodationCommandRepository examAccommodationCommandRepository;
    private final ConfigService configService;

    @Autowired
    public ExamAccommodationServiceImpl(ExamAccommodationQueryRepository examAccommodationQueryRepository, ExamAccommodationCommandRepository examAccommodationCommandRepository, ConfigService configService) {
        this.examAccommodationQueryRepository = examAccommodationQueryRepository;
        this.examAccommodationCommandRepository = examAccommodationCommandRepository;
        this.configService = configService;
    }

    @Override
    public List<ExamAccommodation> findAccommodations(UUID examId, String segmentId, String[] accommodationTypes) {
        return examAccommodationQueryRepository.findAccommodations(examId, segmentId, accommodationTypes);
    }

    @Override
    public List<ExamAccommodation> findAllAccommodations(UUID examId) {
        return examAccommodationQueryRepository.findAccommodations(examId);
    }

    @Override
    public List<ExamAccommodation> initializeExamAccommodations(Exam exam) {
        // This method replaces StudentDLL._InitOpportunityAccommodations_SP.

        // StudentDLL fetches the key accommodations via CommonDLL.TestKeyAccommodations_FN which this call replicates.  The legacy application leverages
        // temporary tables for most of its data structures which is unnecessary in this case so a collection is returned.
        List<Accommodation> assessmentAccommodations = configService.findAssessmentAccommodations(exam.getAssessmentKey());

        // StudentDLL line 6645 - the query filters the results of the temporary table fetched above by these two values.
        // It was decided the record usage and report usage values that are also queried are not actually used.
        List<Accommodation> accommodations = assessmentAccommodations.stream().filter(accommodation ->
            accommodation.isDefaultAccommodation() && accommodation.getDependsOnToolType() == null).collect(Collectors.toList());

        List<ExamAccommodation> examAccommodations = new ArrayList<>();
        accommodations.forEach(accommodation -> {
            ExamAccommodation examAccommodation = new ExamAccommodation.Builder()
                .withExamId(exam.getId())
                .withCode(accommodation.getAccommodationCode())
                .withType(accommodation.getAccommodationType())
                .withDescription(accommodation.getAccommodationValue())
                .withSegmentKey(accommodation.getSegmentKey())
                .build();

            examAccommodations.add(examAccommodation);
        });

        //Inserts the accommodations into the exam system.
        examAccommodationCommandRepository.insert(examAccommodations);

        return examAccommodations;
    }
}
