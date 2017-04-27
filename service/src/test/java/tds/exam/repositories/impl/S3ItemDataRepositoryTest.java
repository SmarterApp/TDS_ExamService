package tds.exam.repositories.impl;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import org.apache.http.client.methods.HttpRequestBase;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import tds.exam.configuration.scoring.ScoringS3Properties;

import java.io.ByteArrayInputStream;

import static com.google.common.base.Charsets.UTF_8;
import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class S3ItemDataRepositoryTest {

    @Mock
    private AmazonS3 mockAmazonS3;

    private ScoringS3Properties scoringS3Properties;

    private S3ItemDataRepository itemReader;

    @Before
    public void setup() {
        scoringS3Properties = random(ScoringS3Properties.class);
        itemReader = new S3ItemDataRepository(mockAmazonS3, scoringS3Properties);
    }

    @Test
    public void itShouldRetrieveAnItemUsingAmazonS3() throws Exception {
        final String itemDataPath = "items/My-Item/My-Item.xml";

        final S3Object response = mock(S3Object.class);
        when(response.getObjectContent()).thenReturn(response("Response Data"));

        when(mockAmazonS3.getObject(any(GetObjectRequest.class))).thenReturn(response);

        final String value = itemReader.findOne(itemDataPath);
        assertThat(value).isEqualTo("Response Data");

        final ArgumentCaptor<GetObjectRequest> objectRequestArgumentCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(mockAmazonS3).getObject(objectRequestArgumentCaptor.capture());

        final GetObjectRequest request = objectRequestArgumentCaptor.getValue();
        assertThat(request.getBucketName()).isEqualTo(scoringS3Properties.getBucketName());
        assertThat(request.getKey()).isEqualTo(scoringS3Properties.getItemPrefix() + itemDataPath);
    }

    @Test
    public void itShouldTrimLongUris() throws Exception {
        final String longPath = "/usr/local/tomcat/resources/tds/bank/items/My-Item/My-Item.xml";

        final S3Object response = mock(S3Object.class);
        when(response.getObjectContent()).thenReturn(response("Response Data"));

        when(mockAmazonS3.getObject(any(GetObjectRequest.class))).thenReturn(response);

        final String value = itemReader.findOne(longPath);
        final ArgumentCaptor<GetObjectRequest> objectRequestArgumentCaptor = ArgumentCaptor.forClass(GetObjectRequest.class);
        verify(mockAmazonS3).getObject(objectRequestArgumentCaptor.capture());

        final GetObjectRequest request = objectRequestArgumentCaptor.getValue();
        assertThat(request.getKey()).isEqualTo(scoringS3Properties.getItemPrefix() + "items/My-Item/My-Item.xml");
    }

    private S3ObjectInputStream response(final String body) throws Exception {
        final ByteArrayInputStream delegate = new ByteArrayInputStream(body.getBytes(UTF_8));
        return new S3ObjectInputStream(delegate, mock(HttpRequestBase.class));
    }
}