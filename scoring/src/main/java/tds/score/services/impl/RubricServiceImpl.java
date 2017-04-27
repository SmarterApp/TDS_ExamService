package tds.score.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tds.score.repositories.RubricRepository;
import tds.score.services.RubricService;

import java.io.IOException;

/**
 * This implementation of a RubricService serves rubric content from a repository.
 */
@Service
public class RubricServiceImpl implements RubricService {

    private final RubricRepository rubricRepository;

    @Autowired
    public RubricServiceImpl(final RubricRepository rubricRepository) {
        this.rubricRepository = rubricRepository;
    }

    @Override
    public String findOne(final String rubricPath) throws IOException {
        return rubricRepository.findOne(rubricPath);
    }
}
