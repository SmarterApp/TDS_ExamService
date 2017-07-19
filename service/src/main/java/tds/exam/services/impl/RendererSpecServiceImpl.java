package tds.exam.services.impl;

import org.springframework.stereotype.Service;

import java.io.IOException;

import tds.itemrenderer.processing.RendererSpecService;
import tds.score.repositories.RendererSpecRepository;

@Service
public class RendererSpecServiceImpl implements RendererSpecService {
    RendererSpecRepository rendererSpecRepository;

    @Override
    public String findOne(String rendererSpecPath) throws IOException {
        return null;
    }
}
