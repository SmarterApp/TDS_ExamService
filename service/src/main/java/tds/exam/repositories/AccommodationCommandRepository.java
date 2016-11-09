package tds.exam.repositories;

import java.util.List;

import tds.exam.ExamAccommodation;

public interface AccommodationCommandRepository {
    void insertAccommodations(List<ExamAccommodation> accommodations);
}
