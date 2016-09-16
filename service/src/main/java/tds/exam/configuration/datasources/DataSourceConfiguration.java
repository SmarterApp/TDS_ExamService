package tds.exam.configuration.datasources;

import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * Configuration for DataSources.  Queries
 */
@Configuration
public class DataSourceConfiguration {
    @Bean(name = "commandDataSource")
    @ConfigurationProperties(prefix = "spring.ds_commands")
    public DataSource commandsDataSource() {
        return DataSourceBuilder
            .create()
            .build();
    }

    @Bean(name = "queryDataSource")
    @ConfigurationProperties(prefix = "spring.ds_queries")
    public DataSource queriesDataSource() {
        return DataSourceBuilder
            .create()
            .build();
    }
}