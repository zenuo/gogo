package zenuo.gogo.core.config;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.TimeoutOptions;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.support.ConnectionPoolSupport;
import lombok.NonNull;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Lettuce配置
 *
 * @author yuanzhen
 * @date 2019/05/08
 */
@Configuration
public class LettuceConfig {
    @Value("${redis.database}")
    private int database;

    @Value("${redis.host}")
    private String host;

    @Value("${redis.port}")
    private int port;

    @Value("${redis.password}")
    private String password;

    @Value("${redis.timeout}")
    private Integer timeout;

    @Value("${redis.pool.max-idle}")
    private int maxIdle;

    @Value("${redis.pool.min-idle}")
    private int minIdle;

    @Value("${redis.pool.max-active}")
    private int maxActive;

    @Value("${redis.pool.max-wait}")
    private long maxWaitMillis;

    @Value("${redis.pool.minEvictableIdleTimeMillis}")
    private Integer minEvictableIdleTimeMillis;

    @Value("${redis.pool.timeBetweenEvictionRunsMillis}")
    private Integer timeBetweenEvictionRunsMillis;

    @Bean
    public GenericObjectPoolConfig getGenericObjectPoolConfig() {
        GenericObjectPoolConfig poolConfig = new GenericObjectPoolConfig();
        poolConfig.setMinIdle(minIdle);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMaxTotal(maxActive);
        poolConfig.setMaxWaitMillis(maxWaitMillis);
        poolConfig.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        poolConfig.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setTestOnBorrow(false);
        poolConfig.setTestOnReturn(false);
        // 不禁止JMX会报错
        poolConfig.setJmxEnabled(false);
        return poolConfig;
    }

    /**
     * 创建Redis连接池
     *
     * @param poolConfig 池配置
     * @return Redis连接池
     */
    @Bean
    public GenericObjectPool<StatefulRedisConnection<String, String>> getPool(@NonNull GenericObjectPoolConfig poolConfig) {
        //单机Redis
        RedisURI redisUri = RedisURI.Builder
                .redis(host, port)
                .withPassword(password)
                .withDatabase(database)
                .build();
        //客户端，若是集群，则使用RedisClusterClient
        final RedisClient client = RedisClient.create(redisUri);
        //配置
        client.setOptions(ClientOptions.builder()
                //自动重连
                .autoReconnect(true)
                //断开连接的行为
                .disconnectedBehavior(ClientOptions.DisconnectedBehavior.DEFAULT)
                //超时配置
                .timeoutOptions(TimeoutOptions.builder()
                        //固定
                        .fixedTimeout(Duration.of(timeout, ChronoUnit.MILLIS)).build())
                .build());
        return ConnectionPoolSupport
                .createGenericObjectPool(client::connect, poolConfig);
    }
}
