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

package tds.exam.configuration.data;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;

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