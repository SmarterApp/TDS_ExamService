package tds.score.services;

import java.io.IOException;

/**
 * Implementations of this interface are responsible for providing scoring rubrics.
 */
public interface RubricService {

    /**
     * Load a scoring rubric by path.
     *
     * @param rubricPath The rubric path
     * @return The rubric contents
     */
    String findOne(final String rubricPath) throws IOException;
}
