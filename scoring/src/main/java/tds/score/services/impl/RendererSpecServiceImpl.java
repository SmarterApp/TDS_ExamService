package tds.score.services.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import tds.itemrenderer.processing.RendererSpecService;
import tds.score.repositories.RendererSpecRepository;

import java.io.IOException;

/**
 * This implementation of an RendererSpecService serves renderer spec content from a repository.
 */
@Service
public class RendererSpecServiceImpl implements RendererSpecService {

    private final RendererSpecRepository repository;

    @Autowired
    public RendererSpecServiceImpl(final RendererSpecRepository repository) {
        this.repository = repository;
    }

    @Override
    public String findOne(final String rendererSpecPath) throws IOException {
        return repository.findOne(rendererSpecPath);
    }
}
