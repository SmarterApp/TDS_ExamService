package tds.exam.repositories;

import java.util.List;

import tds.exam.ExamAccommodation;

public interface ExamAccommodationCommandRepository {
    void insertAccommodations(List<ExamAccommodation> accommodations);
}
