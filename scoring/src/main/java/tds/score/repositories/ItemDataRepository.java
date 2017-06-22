/***************************************************************************************************
 * Copyright 2017 Regents of the University of California. Licensed under the Educational
 * Community License, Version 2.0 (the “license”); you may not use this file except in
 * compliance with the License. You may obtain a copy of the license at
 *
 * https://opensource.org/licenses/ECL-2.0
 *
 * Unless required under applicable law or agreed to in writing, software distributed under the
 * License is distributed in an “AS IS” BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for specific language governing permissions
 * and limitations under the license.
 **************************************************************************************************/

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
