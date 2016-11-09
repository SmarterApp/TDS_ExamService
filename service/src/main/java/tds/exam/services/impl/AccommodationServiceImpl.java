package tds.exam.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import tds.exam.ExamAccommodation;
import tds.exam.repositories.AccommodationQueryRepository;
import tds.exam.services.AccommodationService;

@Service
class AccommodationServiceImpl implements AccommodationService {
    private final AccommodationQueryRepository accommodationQueryRepository;

    @Autowired
    public AccommodationServiceImpl(AccommodationQueryRepository accommodationQueryRepository) {
        this.accommodationQueryRepository = accommodationQueryRepository;
    }

    @Override
    public List<ExamAccommodation> findAccommodations(UUID examId, String segmentId, String[] accommodationTypes) {
        return accommodationQueryRepository.findAccommodations(examId, segmentId, accommodationTypes);
    }
}
