package tds.exam.repositories.impl;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Repository;

import java.io.FileInputStream;
import java.io.IOException;

import tds.score.repositories.ItemDataRepository;

@Repository
public class FilePathItemDataRepository implements ItemDataRepository {
    @Override
    public String findOne(final String itemPath) throws IOException {
        return IOUtils.toString(new FileInputStream(itemPath));
    }
}
