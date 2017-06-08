package tds.exam.repositories.impl;

import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Repository;

import java.io.FileInputStream;
import java.io.IOException;

import tds.score.repositories.RendererSpecRepository;

@Repository
public class FilePathRendererSpecRepository implements RendererSpecRepository {
    @Override
    public String findOne(final String rendererSpecPath) throws IOException {
        return IOUtils.toString(new FileInputStream(rendererSpecPath));
    }
}
