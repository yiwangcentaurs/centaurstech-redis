package com.centaurstech.redis.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(value = {CacheService.class})
public class CacheService extends CacheServiceWrapper {

    @Autowired
    public CacheService(@Qualifier("redisService") RedisService redisService) {
        super(redisService);
    }

}
