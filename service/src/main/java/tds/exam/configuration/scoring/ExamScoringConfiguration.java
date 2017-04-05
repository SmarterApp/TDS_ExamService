package tds.exam.configuration.scoring;

import AIR.Common.Web.HttpWebHelper;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

import java.util.HashMap;
import java.util.Map;

import tds.itemscoringengine.IItemScorer;
import tds.itemscoringengine.IItemScorerManager;
import tds.itemscoringengine.itemscorers.MCItemScorer;
import tds.itemscoringengine.itemscorers.ProxyItemScorer;
import tds.itemscoringengine.itemscorers.QTIItemScorer;
import tds.itemscoringengine.web.server.AppStatsRecorder;
import tds.itemscoringengine.web.server.ScoringMaster;
import tds.score.configuration.ScoringConfiguration;

@Configuration
@Import(
    ScoringConfiguration.class
)
public class ExamScoringConfiguration {
    private static final IItemScorer mciScorer = new MCItemScorer();
    private static final IItemScorer qtiScorer = new QTIItemScorer();
    private static final IItemScorer proxyScorer = new ProxyItemScorer(new HttpWebHelper());

    @Bean
    public IItemScorerManager getScoreManager(final ExamScoringProperties examScoringProperties) {
        Map<String, IItemScorer> scorers = new HashMap<>();

        scorers.put("MC", mciScorer);
        scorers.put("MS", mciScorer);
        scorers.put("QTI", proxyScorer);
        scorers.put("EBSR", qtiScorer);
        scorers.put("HTQ", qtiScorer);
        scorers.put("GI", proxyScorer);
        scorers.put("EQ", proxyScorer);
        scorers.put("TI", qtiScorer);

        return new ScoringMaster(
            scorers,
            new AppStatsRecorder(),
            examScoringProperties.getQueueThreadCount(),
            examScoringProperties.getQueueHiWaterMark(),
            examScoringProperties.getQueueLowWaterMark(),
            examScoringProperties.getPythonScoringUrl(),
            examScoringProperties.getMaxAttempts(),
            examScoringProperties.getSympyTimeoutMillis()
        );
    }

    @Bean
    public AmazonS3 getAmazonS3(final ScoringS3Properties s3Properties) {
        BasicAWSCredentials basicAWSCredentials = new BasicAWSCredentials(s3Properties.getAccessKey(), s3Properties.getSecretKey());

        return AmazonS3ClientBuilder
            .standard()
            .withCredentials(new AWSStaticCredentialsProvider(basicAWSCredentials))
            .withRegion(Regions.US_WEST_2)
            .build();
    }
}
