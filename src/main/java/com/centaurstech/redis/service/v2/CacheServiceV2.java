package com.centaurstech.redis.service.v2;

import com.centaurstech.redis.service.CacheService;
import com.centaurstech.redis.service.CacheServiceWrapper;
import com.centaurstech.redis.service.RedisService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

@Service
@ConditionalOnMissingBean(value = {CacheServiceV2.class})
public class CacheServiceV2 extends CacheServiceWrapper {

    @Autowired
    public CacheServiceV2(@Qualifier("redisServiceV2") RedisService redisService) {
        super(redisService);
    }


}
