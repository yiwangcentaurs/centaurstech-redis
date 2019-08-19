package com.centaurstech.redis.annotation;

import com.centaurstech.redis.configuration.RedisConfig;
import com.centaurstech.redis.service.CacheService;
import com.centaurstech.redis.service.RedisService;
import org.springframework.context.annotation.Import;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

@Retention(value = java.lang.annotation.RetentionPolicy.RUNTIME)
@Target(value = { java.lang.annotation.ElementType.TYPE })
@Documented
@Import({RedisConfig.class, CacheService.class, RedisService.class})
public @interface EnableCentaursRedis {
}
