package com.wanari.multidb.example.configuration;

import liquibase.integration.spring.SpringLiquibase;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceBuilder;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.orm.jpa.EntityManagerFactoryBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.transaction.PlatformTransactionManager;

import javax.persistence.EntityManagerFactory;
import javax.sql.DataSource;

@Configuration
@EnableJpaRepositories(
    basePackages = ViewDatabaseConfiguration.REPOSITORY_PACKAGE,
    entityManagerFactoryRef = ViewDatabaseConfiguration.ENTITY_MANAGER_FACTORY,
    transactionManagerRef = ViewDatabaseConfiguration.TRANSACTION_MANAGER
)
public class ViewDatabaseConfiguration {

    private static final String SPECIFIER = "view";

    private static final String DOMAIN_PACKAGE = "com.wanari.multidb.example.domain." + SPECIFIER;
    static final String REPOSITORY_PACKAGE = "com.wanari.multidb.example.repository." + SPECIFIER;
    private static final String CONFIGURATION_PROPERTIES = "spring.datasource." + SPECIFIER;

    private static final String DATA_SOURCE = SPECIFIER + "DataSource";
    static final String ENTITY_MANAGER_FACTORY = SPECIFIER + "EntityManagerFactory";
    static final String TRANSACTION_MANAGER = SPECIFIER + "TransactionManager";
    private static final String PERSISTENCE_UNIT = SPECIFIER + "PersistenceUnit";

    private static final String LIQUIBASE_CHANGELOG_MASTER_LOCATION = "classpath:liquibase/" + SPECIFIER + "_master.xml";

    @Bean
    public SpringLiquibase liquibase(@Qualifier(DATA_SOURCE) DataSource dataSource) {
        SpringLiquibase liquibase = new SpringLiquibase();
        liquibase.setDataSource(dataSource);
        liquibase.setChangeLog(LIQUIBASE_CHANGELOG_MASTER_LOCATION);
        return liquibase;
    }

    @Primary
    @Bean(name = DATA_SOURCE)
    @ConfigurationProperties(prefix = CONFIGURATION_PROPERTIES)
    public DataSource dataSource() {
        return DataSourceBuilder.create().build();
    }

    @Primary
    @Bean(name = ENTITY_MANAGER_FACTORY)
    public LocalContainerEntityManagerFactoryBean entityManagerFactory(
        EntityManagerFactoryBuilder builder,
        @Qualifier(DATA_SOURCE) DataSource viewDataSource
    ) {
        return builder
            .dataSource(viewDataSource)
            .packages(DOMAIN_PACKAGE)
            .persistenceUnit(PERSISTENCE_UNIT)
            .build();
    }

    @Bean(name = TRANSACTION_MANAGER)
    public PlatformTransactionManager transactionManager(@Qualifier(ENTITY_MANAGER_FACTORY) EntityManagerFactory entityManagerFactory) {
        return new JpaTransactionManager(entityManagerFactory);
    }
}
