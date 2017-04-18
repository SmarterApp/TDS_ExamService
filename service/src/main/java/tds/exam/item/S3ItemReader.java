package tds.exam.item;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import net.logstash.logback.encoder.org.apache.commons.lang.ArrayUtils;
import org.springframework.stereotype.Component;
import tds.exam.configuration.scoring.ScoringS3Properties;
import tds.itemrenderer.processing.ItemDataReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static org.apache.commons.lang3.StringUtils.join;

@Component
public class S3ItemReader implements ItemDataReader {
    private final AmazonS3 s3Client;
    private final ScoringS3Properties s3Properties;

    public S3ItemReader(final AmazonS3 s3Client,
                        final ScoringS3Properties s3Properties) {
        this.s3Client = s3Client;
        this.s3Properties = s3Properties;
    }

    @Override
    public InputStream readData(final URI uri) throws IOException {
        final String itemLocation = s3Properties.getItemPrefix() + trimPath(uri);

        try {
            final S3Object item = s3Client.getObject(new GetObjectRequest(
                s3Properties.getBucketName(), itemLocation));

            if (item == null) {
                throw new IOException("Could not find file for " + itemLocation);
            }

            return item.getObjectContent();
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
     * TODO This should be updated and/or removed once we have full control over the paths coming in.
     *
     * @param uri   A resource uri
     * @return The resource path relative to our S3 bucket and prefix
     */
    private String trimPath(final URI uri) {
        String[] pathElements = uri.getPath().split("/");
        if (pathElements.length < 3) {
            throw new IllegalArgumentException("Resource path must have at least 3 elements: " + uri.toString());
        }
        return join(
            ArrayUtils.subarray(pathElements, pathElements.length-3, pathElements.length),
            "/");
    }
}
