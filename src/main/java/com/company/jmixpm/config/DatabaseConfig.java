package com.company.jmixpm.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.*;
import org.springframework.vault.core.VaultTemplate;

import javax.sql.DataSource;

@Configuration
public class DatabaseConfig {

    private final VaultTemplate vaultTemplate;

    public DatabaseConfig(@Lazy VaultTemplate vaultTemplate) {
        this.vaultTemplate = vaultTemplate;
    }

    @Primary
    @Profile("default")
    @Bean("dataSourcePropertiesDev")
    @ConfigurationProperties("main.datasource")
    DataSourceProperties dataSourceProperties() {
        return new DataSourceProperties();
    }

    @Bean
    @Primary
    @ConfigurationProperties("main.datasource.hikari")
    DataSource dataSource(final DataSourceProperties dataSourceProperties) {
        return dataSourceProperties.initializeDataSourceBuilder().build();
    }

    @Primary
    @Profile("prod")
    @Bean("dataSourcePropertiesProd")
//    @ConfigurationProperties("main.datasource")
//    this annotation should not be here!
    DataSourceProperties dataSourcePropertiesVault() {
        DataSourceProperties properties = new DataSourceProperties();
        DbConfigWrapper dbConfig = new ObjectMapper().convertValue(vaultTemplate.read(
                "secret/data/application/database/credentials"
        ).getData().get("data"), DbConfigWrapper.class);

        properties.setUrl(dbConfig.getUrl());
        properties.setUsername(dbConfig.getUsername());
        properties.setPassword(dbConfig.getPassword());

        return properties;
    }

}
