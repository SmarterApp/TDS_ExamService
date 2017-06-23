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

package tds.exam.configuration.scoring;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import tds.itemscoringengine.IItemScorer;
import tds.itemscoringengine.IItemScorerManager;
import tds.itemscoringengine.itemscorers.MCItemScorer;
import tds.itemscoringengine.itemscorers.QTIItemScorer;
import tds.itemscoringengine.web.server.AppStatsRecorder;
import tds.itemscoringengine.web.server.ScoringMaster;
import tds.score.configuration.ScoringConfiguration;
import tds.score.services.impl.ItemDataServiceImpl;
import tds.score.services.impl.RendererSpecServiceImpl;
import tds.score.services.impl.RubricServiceImpl;

import java.util.HashMap;
import java.util.Map;

@Configuration
@Import({
    ScoringConfiguration.class,
    RubricServiceImpl.class,
    ItemDataServiceImpl.class,
    RendererSpecServiceImpl.class
})
public class ExamScoringConfiguration {
    private static final IItemScorer mciScorer = new MCItemScorer();
    private static final IItemScorer qtiScorer = new QTIItemScorer();

    @Bean
    public IItemScorerManager getScoreManager(final ExamScoringProperties examScoringProperties) {
        Map<String, IItemScorer> scorers = new HashMap<>();

        scorers.put("MC", mciScorer);
        scorers.put("MS", mciScorer);
        scorers.put("MI", qtiScorer);
        scorers.put("QTI", qtiScorer);
        scorers.put("EBSR", qtiScorer);
        scorers.put("HTQ", qtiScorer);
        scorers.put("GI", qtiScorer);
        scorers.put("EQ", qtiScorer);
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
