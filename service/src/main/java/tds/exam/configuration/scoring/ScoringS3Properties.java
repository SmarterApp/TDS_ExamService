package tds.exam.configuration.scoring;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * Contains the properties for s3 connections
 */
@Component
@ConfigurationProperties(prefix = "exam.scoring.s3")
public class ScoringS3Properties {
    private String accessKey;
    private String secretKey;
    private String bucketName;
    private String itemPrefix;

    public String getAccessKey() {
        return accessKey;
    }

    public void setAccessKey(final String accessKey) {
        this.accessKey = accessKey;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(final String secretKey) {
        this.secretKey = secretKey;
    }

    public String getBucketName() {
        return bucketName;
    }

    public void setBucketName(final String bucketName) {
        this.bucketName = bucketName;
    }

    public String getItemPrefix() {
        return itemPrefix;
    }

    public void setItemPrefix(final String itemPrefix) {
        this.itemPrefix = itemPrefix;
    }
}
