package com.centaurstech.redis.service;

import com.centaurstech.redis.domain.TimeBasedCache;
import com.centaurstech.redis.interfaces.RedisKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@ConditionalOnMissingBean(value = {CacheService.class})
public class CacheService {
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    private RedisService redisService;
    private TimeBasedCache<Object> cacheContainer;
    private boolean redisWorking = true;
    private static Long DEFAULT_TIME_OUT = 24 * 3600 * 1000L;//默认缓存为一天

    private static Long DEFAULT_LOCK_TIME_OUT = 5 * 1000L;//默认锁5s
    private static Long DEFAULT_LOCK_RETRY_PERIOD = 100L;//默认100ms重试
    private static Long DEFAULT_LOCK_EXPIRES_PERIOD = 4 * 1000L;//默认100ms重试

    @Autowired
    public CacheService(RedisService redisService) {
        this.redisService = redisService;
        try {
            this.redisService.setObj("testForConnection", 100000L, "ttc");
        } catch (Exception e) {
            this.redisWorking = false;
            cacheContainer = new TimeBasedCache<>(DEFAULT_TIME_OUT);
        }
    }

    private String generateKey(RedisKey table, String key) {
        //全部以"类名:key"作为唯一键 ，所以key要保证唯一，类名相当于表名
        return new StringBuffer(table.getKey()).append(":").append(key).toString();
    }

    /**
     * 从reids或者内存获取对象
     *
     * @param redisTable 给每个对象指定的key前缀，在RedisTable枚举定义，防止不同人使用冲突
     * @param key        对象唯一key
     * @return 返回key对应的对象
     */
    public Object getObj(RedisKey redisTable, String key) {
        Object result = null;
        String currentKey = generateKey(redisTable, key);
        if (this.redisWorking) {
            result = this.redisService.getObj(currentKey);
        } else {
            result = this.cacheContainer.get(currentKey);
        }
        return result;
    }

    /**
     * 参见setObj(redisTable, key, value, timeout)
     *
     * @param redisKey
     * @param key
     * @param value
     */
    public void setObjWithoutTimeout(RedisKey redisKey, String key, Object value) {
        String currentKey = generateKey(redisKey, key);
        if (this.redisWorking) {
            this.redisService.setObj(currentKey, value);
        } else {
            this.cacheContainer.put(currentKey, value);
        }
    }

    /**
     * 参见setObj(redisTable, key, value, timeout)
     *
     * @param redisTable
     * @param key
     * @param value
     */
    public void setObj(RedisKey redisTable, String key, Object value) {
        setObj(redisTable, key, value, null);
    }

    /**
     * 向redis或者内存放入对象
     *
     * @param redisTable 给每个对象指定的key前缀，在RedisTable枚举定义，防止不同人使用冲突
     * @param key        对象唯一key
     * @param value      需要放入的对象
     * @param timeout    缓存时间，单位毫秒
     */
    public Object setObj(RedisKey redisTable, String key, Object value, Long timeout) {
        String currentKey = generateKey(redisTable, key);
        if (timeout == null) {
            timeout = DEFAULT_TIME_OUT;
        }
        if (this.redisWorking) {
            this.redisService.setObj(currentKey, timeout, value);
        } else {
            this.cacheContainer.put(currentKey, value, timeout);
        }
        return value;
    }

    /**
     * 删除redis或者内存中对应key的对象
     *
     * @param redisTable 给每个对象指定的key前缀，在RedisTable枚举定义，防止不同人使用冲突
     * @param key        对象唯一key
     */
    public void delKey(RedisKey redisTable, String key) {
        String currentKey = generateKey(redisTable, key);
        if (this.redisWorking) {
            this.redisService.deleteKey(currentKey);
        } else {
            this.cacheContainer.put(currentKey, null);
        }
    }

    /**
     * 判断redis或者内存中是否包含指定key
     *
     * @param redisTable 给每个对象指定的key前缀，在RedisTable枚举定义，防止不同人使用冲突
     * @param key        对象唯一key
     * @return 存在true，不存在false
     */
    public boolean containKey(RedisKey redisTable, String key) {
        String currentKey = generateKey(redisTable, key);
        boolean result = false;
        if (this.redisWorking) {
            result = this.redisService.existsKey(currentKey);
        } else {
            result = this.cacheContainer.contains(key);
        }
        return result;
    }

    /**
     * 判断redis或者内存中是否包含指定key，否则向redis或者内存放入对象
     *
     * @param redisTable
     * @param key
     * @param value
     * @param timeout
     * @return 如果已存在，返回null。否则返回放入的value
     */
    public synchronized Object setObjIfNotContainKey(RedisKey redisTable, String key, Object value, Long timeout) {
        if (containKey(redisTable, key)) {
            return null;
        }
        return setObj(redisTable, key, value, timeout);
    }

    /**
     * 右侧入队元素到队列
     *
     * @param redisKey
     * @param key
     * @param value
     */
    public void rPushObj(RedisKey redisKey, String key, Object value) {
        if (redisWorking) {
            String currentKey = generateKey(redisKey, key);
            this.redisService.rPushObj(currentKey, value);
        }
    }

    /**
     * 左侧入队元素到队列
     *
     * @param redisKey
     * @param key
     * @param value
     */
    public void lPushObj(RedisKey redisKey, String key, Object value) {
        if (redisWorking) {
            String currentKey = generateKey(redisKey, key);
            this.redisService.lPushObj(currentKey, value);
        }
    }

    /**
     * 左侧出队元素
     *
     * @param redisKey
     * @param key
     * @return
     */
    public Object lPopObj(RedisKey redisKey, String key) {
        if (redisWorking) {
            String currentKey = generateKey(redisKey, key);
            return this.redisService.lPopObj(currentKey);
        } else {
            return null;
        }
    }

    /**
     * 右侧出队元素
     *
     * @param redisKey
     * @param key
     * @return
     */
    public Object rPopObj(RedisKey redisKey, String key) {
        if (redisWorking) {
            String currentKey = generateKey(redisKey, key);
            return this.redisService.rPopObj(currentKey);
        } else {
            return null;
        }
    }

    /**
     * 获取左侧第一个元素，不移除
     *
     * @param redisKey
     * @param key
     * @return
     */
    public Object lPeekObj(RedisKey redisKey, String key) {
        if (redisWorking) {
            String currentKey = generateKey(redisKey, key);
            return this.redisService.lPeekObj(currentKey);
        } else {
            return null;
        }
    }

    /**
     * 获取右侧第一个元素，不移除
     *
     * @param redisKey
     * @param key
     * @return
     */
    public Object rPeekObj(RedisKey redisKey, String key) {
        if (redisWorking) {
            String currentKey = generateKey(redisKey, key);
            return this.redisService.rPeekObj(currentKey);
        } else {
            return null;
        }
    }

    /**
     * 获取list
     *
     * @param redisKey
     * @param key
     * @return
     */
    public List<Object> getList(RedisKey redisKey, String key) {
        if (redisWorking) {
            String currentKey = generateKey(redisKey, key);
            return this.redisService.getList(currentKey);
        } else {
            return null;
        }
    }

    /**
     * 获取list大小
     *
     * @param redisKey
     * @param key
     * @return
     */
    public Long getListSize(RedisKey redisKey, String key) {
        if (redisWorking) {
            String currentKey = generateKey(redisKey, key);
            return this.redisService.listSize(currentKey);
        } else {
            return 0L;
        }
    }

    public void setList(RedisKey redisKey, String key, List<Object> objs) {
        if (redisWorking) {
            String currentKey = generateKey(redisKey, key);
            this.redisService.setList(currentKey, objs);
        }
    }

    public Object lPopAndRPush(RedisKey redisKey, String key) {
        if (redisWorking) {
            String currentKey = generateKey(redisKey, key);
            return this.redisService.lPopAndRightPush(currentKey);
        } else {
            return null;
        }
    }

    public Object lPopAndRPushToAnother(RedisKey redisKey, String sourceKey, String destinationKey) {
        if (redisWorking) {
            String currentSourceKey = generateKey(redisKey, sourceKey);
            String currentDestinationKey = generateKey(redisKey, destinationKey);
            return this.redisService.lPopAndRPushToAnother(currentSourceKey, currentDestinationKey);
        } else {
            return null;
        }
    }

    /**
     * 从list中移除count个符合的obj
     *
     * @param redisKey
     * @param key
     * @param count
     * @param obj
     * @return
     */
    public Long removeFromList(RedisKey redisKey, String key, long count, Object obj) {
        if (redisWorking) {
            String currentKey = generateKey(redisKey, key);
            return this.redisService.removeFromList(currentKey, count, obj);
        } else {
            return null;
        }
    }

    /**
     * 判断list中是否包含某元素
     *
     * @param redisKey
     * @param key
     * @param obj
     * @return
     */
    public boolean listContainObj(RedisKey redisKey, String key, Object obj) {
        if (redisWorking) {
            String currentKey = generateKey(redisKey, key);
            return this.redisService.listContainObj(currentKey, obj);
        } else {
            return false;
        }
    }

    /**
     * 加锁
     *
     * @param redisKey
     * @param key
     * @param timeout     用户等待过期时间
     * @param expireIn    锁失效时间
     * @param retryPeriod 重试周期
     * @return
     */
    public boolean lock(RedisKey redisKey, String key, Long timeout, Long expireIn, Long retryPeriod) {
        if (redisWorking) {
            String currentKey = generateKey(redisKey, key);
            long begin = System.currentTimeMillis();
            do {
                boolean success = redisService.setIfAbsent(currentKey, 1, expireIn);
                if (success) {
                    /**
                     * 锁成功了
                     */
                    return success;
                } else {
                    /**
                     * 未锁成功
                     * 1.等待并重试
                     */
                    try {
                        Thread.sleep(retryPeriod);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
                if (System.currentTimeMillis() - begin >= timeout) {
                    return false;
                }
            } while (true);
        } else {
            return false;
        }
    }

    /**
     * 默认 等待时间，重试时间 加锁
     *
     * @param redisKey
     * @param key
     * @param expireIn 锁定时间
     * @return
     */
    public boolean lock(RedisKey redisKey, String key, Long expireIn) {
        return lock(redisKey, key, DEFAULT_LOCK_TIME_OUT, expireIn, DEFAULT_LOCK_RETRY_PERIOD);
    }

    /**
     * 默认 等待时间，锁时间，重试时间 加锁
     *
     * @param redisKey
     * @param key
     * @return
     */
    public boolean lock(RedisKey redisKey, String key) {
        return lock(redisKey, key, DEFAULT_LOCK_TIME_OUT, DEFAULT_LOCK_EXPIRES_PERIOD, DEFAULT_LOCK_RETRY_PERIOD);
    }


    /**
     * 解锁
     *
     * @param redisKey
     * @param key
     * @return
     */
    public boolean unlock(RedisKey redisKey, String key) {
        if (redisWorking) {
            String currentKey = generateKey(redisKey, key);
            return this.redisService.deleteKey(currentKey);
        } else {
            return false;
        }
    }

}
