package tds.exam.item;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import tds.exam.configuration.scoring.ScoringS3Properties;
import tds.itemrenderer.processing.ItemDataReader;

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
        final String itemLocation = s3Properties.getItemPrefix() + uri.toString();
        S3Object item = s3Client.getObject(new GetObjectRequest(
            s3Properties.getBucketName(), itemLocation));

        if(item == null) {
            throw new IOException("Could not find file for " + itemLocation);
        }

        return item.getObjectContent();
    }
}
