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
import tds.score.repositories.RendererSpecRepository;

import static net.logstash.logback.encoder.org.apache.commons.lang.ArrayUtils.subarray;
import static org.apache.commons.lang3.StringUtils.join;

/**
 * This implementation of a RendererSpecRepository reads information on S3.
 */
@Repository
@Primary
public class S3RendererSpecRepository implements RendererSpecRepository {

    private final AmazonS3 s3Client;
    private final ScoringS3Properties s3Properties;

    S3RendererSpecRepository(final AmazonS3 s3Client,
                             final ScoringS3Properties s3Properties) {
        this.s3Client = s3Client;
        this.s3Properties = s3Properties;
    }

    @Override
    public String findOne(final String rendererSpecPath) throws IOException {
        final String rendererLocation = s3Properties.getItemPrefix() + buildPath(rendererSpecPath);

        try {
            final S3Object item = s3Client.getObject(new GetObjectRequest(
                s3Properties.getBucketName(), rendererLocation));

            if (item == null) {
                throw new IOException("Could not find file for " + rendererLocation);
            }

            return IOUtils.toString(item.getObjectContent());
        } catch (final AmazonS3Exception ex) {
            throw new IOException("Unable to read S3 renderer spec: " + rendererLocation, ex);
        }
    }

    /**
     * This is a fragile path trimmer that reduces resource paths from something like:
     * /usr/local/tomcat/resources/tds/bank/items/Item-187-1576/Item_1576_v5.eax
     * to
     * items/Item-187-1576/Item_1576_v5.eax
     *
     * @param resourcePath   A resource path
     * @return The resource path relative to our S3 bucket and prefix
     */
    private String buildPath(final String resourcePath) {
        final String[] pathElements = resourcePath.split("/");
        return "items/" + join(subarray(pathElements, pathElements.length-2, pathElements.length), "/");
    }
}
