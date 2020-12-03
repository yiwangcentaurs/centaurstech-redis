package com.centaurstech.redis.service.v2;

import com.centaurstech.redis.annotation.EnableCentaursRedis;
import com.centaurstech.redis.service.RedisService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.data.redis.core.*;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@Qualifier("redisServiceV2")
@ConditionalOnMissingBean(value = {RedisServiceV2.class})
public class RedisServiceV2 extends RedisService {
    @Autowired
    public RedisServiceV2(@Qualifier("redisTemplateV2") RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate);
    }

}
