package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import tds.exam.ExamAccommodation;
import tds.exam.repositories.ExamAccommodationQueryRepository;
import tds.exam.services.ExamAccommodationService;

@Service
class ExamAccommodationServiceImpl implements ExamAccommodationService {
    private final ExamAccommodationQueryRepository examAccommodationQueryRepository;

    @Autowired
    public ExamAccommodationServiceImpl(ExamAccommodationQueryRepository examAccommodationQueryRepository) {
        this.examAccommodationQueryRepository = examAccommodationQueryRepository;
    }

    @Override
    public List<ExamAccommodation> findAccommodations(UUID examId, String segmentId, String[] accommodationTypes) {
        return examAccommodationQueryRepository.findAccommodations(examId, segmentId, accommodationTypes);
    }
}
