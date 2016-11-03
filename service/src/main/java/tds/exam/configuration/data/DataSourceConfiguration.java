package tds.exam.configuration.data;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

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

    @Bean(name = "commandJdbcTemplate")
    public NamedParameterJdbcTemplate commandJdbcTemplate() {
        return new NamedParameterJdbcTemplate(commandsDataSource());
    }

    @Bean(name = "queryJdbcTemplate")
    public NamedParameterJdbcTemplate queryJdbcTemplate() {
        return new NamedParameterJdbcTemplate(queriesDataSource());
    }

    @Bean
    public PlatformTransactionManager transactionManager(@Qualifier("commandDataSource") DataSource dataSource) {
        final DataSourceTransactionManager dataSourceTransactionManager = new DataSourceTransactionManager();
        dataSourceTransactionManager.setDataSource(dataSource);
        return dataSourceTransactionManager;
    }
}