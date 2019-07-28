package com.mycompany.movieservice.config;

import com.zaxxer.hikari.HikariConfigMXBean;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.HikariPoolMXBean;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.jdbc.DataSourceBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.vault.core.lease.SecretLeaseContainer;
import org.springframework.vault.core.lease.event.SecretLeaseCreatedEvent;

import javax.annotation.PostConstruct;
import javax.sql.DataSource;

@Slf4j
@Configuration
public class DatasourceConfig {

    @Value("${datasource.vault-creds-path}")
    private String vaultCredsPath;

    private final ApplicationContext applicationContext;
    private final SecretLeaseContainer leaseContainer;

    public DatasourceConfig(ApplicationContext applicationContext, SecretLeaseContainer leaseContainer) {
        this.applicationContext = applicationContext;
        this.leaseContainer = leaseContainer;
    }

    @PostConstruct
    private void postConstruct() {
        leaseContainer.addLeaseListener(event -> {
            if (event instanceof SecretLeaseCreatedEvent && vaultCredsPath.equals(event.getSource().getPath())) {
                log.info("Received event: {}", event);

                String username = applicationContext.getEnvironment().getProperty("datasource.username");
                String password = applicationContext.getEnvironment().getProperty("datasource.password");

                log.info("==> datasource.username: {}", username);

                updateDataSource(username, password);
            }
        });
    }

    private void updateDataSource(String username, String password) {
        HikariDataSource hikariDataSource = (HikariDataSource) applicationContext.getBean("dataSource");

        log.info("Soft evict database connections");
        HikariPoolMXBean hikariPoolMXBean = hikariDataSource.getHikariPoolMXBean();
        if (hikariPoolMXBean != null) {
            hikariPoolMXBean.softEvictConnections();
        }

        log.info("Update database credentials");
        HikariConfigMXBean hikariConfigMXBean = hikariDataSource.getHikariConfigMXBean();
        hikariConfigMXBean.setUsername(username);
        hikariConfigMXBean.setPassword(password);
    }

    @Bean
    @ConfigurationProperties(prefix = "datasource")
    DataSource dataSource() {
        String username = applicationContext.getEnvironment().getProperty("datasource.username");
        String password = applicationContext.getEnvironment().getProperty("datasource.password");

        log.info("==> datasource.username: {}", username);

        DataSourceBuilder dataSourceBuilder = DataSourceBuilder.create();
        dataSourceBuilder.username(username);
        dataSourceBuilder.password(password);
        return dataSourceBuilder.build();
    }

}
