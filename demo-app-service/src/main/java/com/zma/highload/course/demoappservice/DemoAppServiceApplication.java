package com.zma.highload.course.demoappservice;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisSentinelConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

@SpringBootApplication
public class DemoAppServiceApplication {

    @Autowired
    private RedisProperties redisProperties;

    public static void main(String[] args) {
        SpringApplication.run(DemoAppServiceApplication.class, args);
    }

    @Bean
    JedisConnectionFactory redisConnectionFactory() {
        RedisSentinelConfiguration redisSentinelConfiguration = new RedisSentinelConfiguration();
        redisSentinelConfiguration.master(redisProperties.getSentinel().getMaster());
        for (String node : redisProperties.getSentinel().getNodes()) {
            String[] props = node.split(":");
            redisSentinelConfiguration.sentinel(props[0], Integer.parseInt(props[1]));
        }
        return new JedisConnectionFactory(redisSentinelConfiguration);
    }

    @Bean
    public StringRedisTemplate redisTemplate() {
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        redisTemplate.setConnectionFactory(redisConnectionFactory());
        return redisTemplate;
    }
}
