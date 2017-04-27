package tds.score.repositories;

import java.io.IOException;

/**
 * Implementations of this interface are responsible for providing Renderer Spec data.
 */
public interface RendererSpecRepository {

    /**
     * Load a Renderer Spec definition by path.
     *
     * @param rendererSpecPath The renderer spec resource path
     * @return The renderer spec contents
     */
    String findOne(final String rendererSpecPath) throws IOException;
}
