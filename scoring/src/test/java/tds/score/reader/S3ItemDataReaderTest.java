package tds.score.reader;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.S3Object;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class S3ItemDataReaderTest {
    @Mock
    private AmazonS3 mockAmazonS3;

    private S3ItemDataReader reader;

    @Before
    public void setUp() {
        reader = new S3ItemDataReader(mockAmazonS3, "bucket", "local/item/");
    }

    @Test(expected = IOException.class)
    public void shouldThrowIfObjectCannotBeFound() throws URISyntaxException, IOException {
        reader.readData(new URI("data"));
    }

    @Test
    public void itShouldStreamData() throws URISyntaxException, IOException {
        InputStream is = mock(InputStream.class);
        S3Object s3Object = new S3Object();
        s3Object.setObjectContent(is);

        when(mockAmazonS3.getObject("bucket", "local/item/data")).thenReturn(s3Object);
        assertThat(reader.readData(new URI("data"))).isEqualTo(s3Object.getObjectContent());
    }

}