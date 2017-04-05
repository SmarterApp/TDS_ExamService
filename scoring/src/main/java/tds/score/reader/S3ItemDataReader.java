package tds.score.reader;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

import tds.itemrenderer.processing.ItemDataReader;

public class S3ItemDataReader implements ItemDataReader {
    private final AmazonS3 s3Client;
    private final String bucket;
    private final String defaultPrefix;

    public S3ItemDataReader(final AmazonS3 s3Client, final String bucket, final String defaultPrefix) {
        this.s3Client = s3Client;
        this.bucket = bucket;
        this.defaultPrefix = defaultPrefix;
    }

    @Override
    public InputStream readData(final URI uri) throws IOException {
        String key = defaultPrefix + uri.toASCIIString();

        S3Object obj = s3Client.getObject(bucket, key);

        if (obj == null) {
            throw new IOException("Unable to find data for " + uri);
        }

        return obj.getObjectContent();
    }
}
