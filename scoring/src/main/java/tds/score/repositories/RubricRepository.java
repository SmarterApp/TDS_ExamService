package tds.score.repositories;

import java.io.IOException;

/**
 * Implementations of this interface are responsible for providing scoring rubrics.
 */
public interface RubricRepository {

    /**
     * Load a rubric definition by path.
     *
     * @param rubricPath    The rubric path
     * @return  The rubric
     */
    String findOne(final String rubricPath) throws IOException;
}
