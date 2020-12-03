package com.centaurstech.redis.annotation;

import com.centaurstech.redis.configuration.RedisConfig;
import com.centaurstech.redis.service.CacheService;
import com.centaurstech.redis.service.RedisService;
import com.centaurstech.redis.service.v2.CacheServiceV2;
import com.centaurstech.redis.service.v2.RedisServiceV2;
import org.springframework.context.annotation.Import;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = {java.lang.annotation.ElementType.TYPE})
@Documented
@Import({RedisConfig.class, CacheService.class, CacheServiceV2.class, RedisService.class, RedisServiceV2.class,RedisTemplate.class})
public @interface EnableCentaursRedis {

}
