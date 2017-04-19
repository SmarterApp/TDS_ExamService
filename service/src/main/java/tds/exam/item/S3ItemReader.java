package tds.exam.item;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.AmazonS3Exception;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.stereotype.Component;
import tds.exam.configuration.scoring.ScoringS3Properties;
import tds.itemrenderer.processing.ItemDataReader;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import static org.apache.commons.io.FilenameUtils.getName;
import static org.apache.commons.io.FilenameUtils.removeExtension;
import static org.apache.commons.lang3.StringUtils.capitalize;

@Component
public class S3ItemReader implements ItemDataReader {
    private final AmazonS3 s3Client;
    private final ScoringS3Properties s3Properties;

    S3ItemReader(final AmazonS3 s3Client,
                        final ScoringS3Properties s3Properties) {
        this.s3Client = s3Client;
        this.s3Properties = s3Properties;
    }

    @Override
    public InputStream readData(final URI uri) throws IOException {
        final String itemLocation = s3Properties.getItemPrefix() + buildPath(uri);

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
     * @param uri   A resource uri
     * @return The resource path relative to our S3 bucket and prefix
     */
    private String buildPath(final URI uri) {
        final String itemName = getName(uri.toString());
        final String dirName = capitalize(removeExtension(itemName));
        return "items/" + dirName + "/" + itemName;
    }
}
