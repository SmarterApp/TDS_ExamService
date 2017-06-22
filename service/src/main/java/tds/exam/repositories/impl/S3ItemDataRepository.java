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

package tds.exam.repositories.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.apache.commons.io.IOUtils;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import java.io.IOException;

import tds.exam.configuration.scoring.ScoringS3Properties;
import tds.score.repositories.ItemDataRepository;

import static org.apache.commons.io.Charsets.UTF_8;
import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.apache.commons.lang3.StringUtils.capitalize;

@Repository
@Primary
public class S3ItemDataRepository implements ItemDataRepository {
    private final AmazonS3 s3Client;
    private final ScoringS3Properties s3Properties;

    S3ItemDataRepository(final AmazonS3 s3Client,
                         final ScoringS3Properties s3Properties) {
        this.s3Client = s3Client;
        this.s3Properties = s3Properties;
    }

    @Override
    public String findOne(final String itemDataPath) throws IOException {
        final String itemLocation = s3Properties.getItemPrefix() + buildPath(itemDataPath);

        try {
            final S3Object item = s3Client.getObject(new GetObjectRequest(
                s3Properties.getBucketName(), itemLocation));

            if (item == null) {
                throw new IOException("Could not find file for " + itemLocation);
            }

            return IOUtils.toString(item.getObjectContent(), UTF_8);
        } catch (final AmazonS3Exception ex) {
            throw new IOException("Unable to read S3 item: " + itemLocation, ex);
        }
    }

    /**
     * This is a fragile path trimmer that reduces resource paths from something like:
     * /usr/local/tomcat/resources/tds/bank/items/Item-187-2501/item-187-2501.xml
     * to
     * items/Item-187-2501/item-187-2501.xml
     *
     * @param itemDataPath   The item resource path
     * @return The resource path relative to our S3 bucket and prefix
     */
    private String buildPath(final String itemDataPath) {
        final String itemName = getName(itemDataPath);
        final String dirName = capitalize(removeExtension(itemName));
        return "items/" + dirName + "/" + itemName;
    }
}
