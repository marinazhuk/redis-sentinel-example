package com.zma.highload.course.demoappservice.repository;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zma.highload.course.demoappservice.model.Product;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Service
public class CacheClientWrapper {
    //The parameter beta can be set to greater than 1.0 to favor earlier recomputation or lesser to favor later.
    // The default 1.0 is optimal for most use cases.
    private static final long BETA = 1;
    private static final long DELTA = 1; // measured duration of the recomputation
    private static final Logger logger = LoggerFactory.getLogger(CacheClientWrapper.class);

    private static final int TTL_IN_SECONDS = 10;

    private final StringRedisTemplate redisTemplate;

    private final ProductRepository productRepository;

    private final ObjectMapper objectMapper;


    public CacheClientWrapper(StringRedisTemplate redisTemplate, ProductRepository productRepository, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.productRepository = productRepository;
        this.objectMapper = objectMapper;
    }

    public Product fetchWithProbabilisticEarlyExpiration(String key) throws JsonProcessingException {
        String productString = redisTemplate.opsForValue().get(key);


        if (productString == null || shouldEarlyRecompute(key)) {
            return recompute(key);
        }

        return objectMapper.readValue(productString, Product.class);
    }

    private boolean shouldEarlyRecompute(String key) {
        long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);

        long currentTimestampInSeconds = System.currentTimeMillis();

        long cacheExpiresAt = currentTimestampInSeconds + expire;

        double xfetch = DELTA * BETA * Math.log(Math.random());

        boolean isEarlyRecomputeRequired = (currentTimestampInSeconds - Math.round(xfetch)) >= cacheExpiresAt;
        if (isEarlyRecomputeRequired) {
            logger.info("Probabilistic early recompute starts {} s before expiration for key '{}' ", key, expire);
        }
        return isEarlyRecomputeRequired;
    }
    private Product recompute(String key) throws JsonProcessingException {
        Product product = productRepository.findById(Long.valueOf(key)).get();

        saveProduct(key, product);

        return product;
    }

    public void saveProduct(String key, Product product) throws JsonProcessingException {
        String productJsonRepresentation = objectMapper.writeValueAsString(product);

        redisTemplate.opsForValue().set(key, productJsonRepresentation, Duration.ofSeconds(TTL_IN_SECONDS));
    }
}
