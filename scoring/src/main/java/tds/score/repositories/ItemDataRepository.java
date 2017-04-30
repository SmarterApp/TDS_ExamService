package tds.score.repositories;

import java.io.IOException;

/**
 * Implementations of this interface are responsible for providing exam item data.
 */
public interface ItemDataRepository {

    /**
     * Load an exam item by path.
     *
     * @param itemPath  The item path
     * @return  The exam item xml content
     */
    String findOne(final String itemPath) throws IOException;
}
