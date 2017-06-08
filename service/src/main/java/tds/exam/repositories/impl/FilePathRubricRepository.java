package tds.exam.repositories.impl;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Repository;

import java.io.FileInputStream;
import java.io.IOException;

import tds.score.repositories.RubricRepository;

@Repository
public class FilePathRubricRepository implements RubricRepository {
    @Override
    public String findOne(final String rubricPath) throws IOException {
        return IOUtils.toString(new FileInputStream(rubricPath));
    }
}
