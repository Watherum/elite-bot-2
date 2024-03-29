package gg.watherum.elitebot.configuration;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.beans.PropertyVetoException;
import java.util.HashMap;
import java.util.Map;

@EnableTransactionManagement
@Configuration
public class DatabaseConfig
{
    private static final Logger LOG = LogManager.getLogger(DatabaseConfig.class);

    /** PostgreSQL Database Configuration */
    @Value("${postgresdb.driver}") private String dbDriver;
    @Value("${postgresdb.type}")  private String dbType;
    @Value("${postgresdb.host}")  private String dbHost;
    @Value("${postgresdb.name}")  private String dbName;
    @Value("${postgresdb.user}")  private String dbUser;
    @Value("${postgresdb.password}")  private String dbPass;
    @Value("${c3p0.pool.size.initial:15}") private int poolInitialSize;
    @Value("${c3p0.pool.size.min:8}") private int poolMinSize;
    @Value("${c3p0.pool.size.max:15}") private int poolMaxSize;

    @Bean(name = "localJdbcTemplate")
    public JdbcTemplate jdbcTemplate() {
        return new JdbcTemplate(postgresDataSource());
    }

    @Bean(name = "localNamedJdbcTemplate")
    public NamedParameterJdbcTemplate namedJdbcTemplate() {
        return new NamedParameterJdbcTemplate(postgresDataSource());
    }

    @Bean(name = "schemaMap")
    public Map<String, String> schemaMap() {
        Map<String, String> schemaMap = new HashMap<>();
        schemaMap.put("statsSchema", "stats");
        return schemaMap;
    }

    /**
     * Configures the sql data source using a connection pool.
     * @return DataSource
     */
    @Bean(destroyMethod = "close")
    public ComboPooledDataSource postgresDataSource() {
        final String jdbcUrlTemplate = "jdbc:%s//%s/%s";
        ComboPooledDataSource pool = new ComboPooledDataSource();
        try {
            pool.setDriverClass(dbDriver);
        }
        catch (PropertyVetoException ex) {
            LOG.error("Error when setting the database driver " + dbDriver + "{}", ex.getMessage());
        }
        pool.setJdbcUrl(String.format(jdbcUrlTemplate, dbType, dbHost, dbName));
        LOG.info("Connecting to Postgres: " + pool.getJdbcUrl());
        pool.setUser(dbUser);
        pool.setPassword(dbPass);
        pool.setInitialPoolSize(poolInitialSize);
        pool.setMinPoolSize(poolMinSize);
        pool.setMaxPoolSize(poolMaxSize);
        // Release connections above minimum if idle for 3 min (180 seconds).
        pool.setMaxIdleTimeExcessConnections(180);
        pool.setAcquireIncrement(4);

        // Test each connection every 60 sec after first check-in
        pool.setTestConnectionOnCheckout(false);
        pool.setTestConnectionOnCheckin(true);
        pool.setIdleConnectionTestPeriod(60);
        // Fast query to execute when testing connections
        pool.setPreferredTestQuery("SELECT 1");
        return pool;
    }

    /**
     * Configures a Spring transaction manager for the postgres data source.
     * @return PlatformTransactionManager
     */
    @Bean
    public PlatformTransactionManager transactionManager() {
        return new DataSourceTransactionManager(postgresDataSource());
    }
}
