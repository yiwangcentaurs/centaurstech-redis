package com.centaurstech.redis.service;

import com.centaurstech.redis.domain.TimeBasedCache;
import com.centaurstech.redis.interfaces.RedisKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Set;

public abstract class CacheServiceWrapper {
    private static final Logger logger = LoggerFactory.getLogger(CacheServiceWrapper.class);
    private RedisService redisService;
    private TimeBasedCache<Object> cacheContainer;
    private boolean redisWorking = true;
    private static Long DEFAULT_TIME_OUT = 24 * 3600 * 1000L;//默认缓存为一天

    public CacheServiceWrapper(RedisService redisService) {
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
    public Object getObj(RedisKey redisTable, String key, boolean logKey) {
        Object result = null;
        String currentKey = generateKey(redisTable, key);
        if (logKey) {
            logger.debug("get key is: " + key);
            logger.debug("get currentKey is: " + currentKey);
        }
        if (this.redisWorking) {
            result = this.redisService.getObj(currentKey);
        } else {
            result = this.cacheContainer.get(currentKey);
        }
        return result;
    }


    public <T> T getObj(RedisKey redisTable, String key, Class<T> returnType, boolean logKey) {
        Object result = null;
        String currentKey = generateKey(redisTable, key);
        if (logKey) {
            logger.debug("get key is: " + key);
            logger.debug("get currentKey is: " + currentKey);
        }
        if (this.redisWorking) {
            result = this.redisService.getObj(currentKey);
        } else {
            result = this.cacheContainer.get(currentKey);
        }
        return (T) result;
    }

    public Object getObj(RedisKey redisTable, String key) {
        return getObj(redisTable, key, false);
    }

    public <T> T getObj(RedisKey redisTable, String key, Class<T> returnType) {
        return getObj(redisTable, key, returnType, false);
    }

    /**
     * 在已知存的是String Enum.name()情况下取出
     *
     * @param <V>
     * @param redisTable
     * @param key
     * @param clazz      来自YourEnum.class
     * @return
     */
    public <V extends Enum<V>> V getEnum(RedisKey redisTable, String key, Class<V> clazz) {
        Object object = this.getObj(redisTable, (String) key);
        String name = (String) object;
        V v = null;
        try {
            v = name != null ? Enum.valueOf(clazz, name) : null;
        } catch (Exception e) {
            logger.error("Error casting from {} to Enum {}", name, clazz.getName());
        }
        return v;
    }

    /**
     * 约定为存为String Enum.name()
     *
     * @param <V>
     * @param redisTable
     * @param key
     * @param v
     */
    public <V extends Enum<V>> void setEnum(RedisKey redisTable, String key, V v, Long timeout) {
        setObj(redisTable, key, v.name(), timeout);
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
        return setObj(redisTable, key, value, timeout, false);
    }

    public <T> T setObjV2(RedisKey redisTable, String key, T value, Long timeout) {
        return setObjV2(redisTable, key, value, timeout, false);
    }

    public Object setObj(RedisKey redisTable, String key, Object value, Long timeout, boolean logKey) {
        String currentKey = generateKey(redisTable, key);
        if (timeout == null) {
            timeout = DEFAULT_TIME_OUT;
        }
        if (logKey) {
            logger.debug("put key is: " + key);
            logger.debug("put currentKey is: " + currentKey);
        }
        if (this.redisWorking) {
            this.redisService.setObj(currentKey, timeout, value);
        } else {
            this.cacheContainer.put(currentKey, value, timeout);
        }

        return value;
    }

    public <T> T setObjV2(RedisKey redisTable, String key, T value, Long timeout, boolean logKey) {
        String currentKey = generateKey(redisTable, key);
        if (timeout == null) {
            timeout = DEFAULT_TIME_OUT;
        }
        if (logKey) {
            logger.debug("put key is: " + key);
            logger.debug("put currentKey is: " + currentKey);
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
     * 删除同一RedisKey下的所有key
     *
     * @param redisTable
     * @return
     */
    public Long delAllKey(RedisKey redisTable) {
        String pattern = generateKey(redisTable, "*");
        if (this.redisWorking) {
            Set<String> keys = this.redisService.findKeys(pattern);
            return this.redisService.deleteKey(keys);
        } else {
            // FIXME
            logger.warn("UnsupportedOperation: delAllKeyByPattern when redisWorking == false");
            return 0L;
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
            result = this.cacheContainer.contains(currentKey);
        }
        return result;
    }

    /**
     * 先取出，再重新存入
     *
     * @param redisTable
     * @param key
     * @param timeout
     */
    public void refreshObj(RedisKey redisTable, String key, Long timeout) {
        if (containKey(redisTable, key)) {
            Object value = getObj(redisTable, key);
            setObj(redisTable, key, value, timeout, false);
        }
    }

    public Object removeObj(RedisKey redisTable, String key) {
        Object value = getObj(redisTable, key);
        delKey(redisTable, key);
        return value;
    }

    /**
     * 移除对应key的元素
     *
     * @param redisTable
     * @param key
     * @param returnType
     * @param <T>
     * @return
     */
    public <T> T removeObj(RedisKey redisTable, String key, Class<T> returnType) {
        T value = getObj(redisTable, key, returnType);
        delKey(redisTable, key);
        return value;
    }

    public void clear() {
        if (this.redisWorking) {
            // not allow clear
        } else {
            this.cacheContainer = new TimeBasedCache<>(DEFAULT_TIME_OUT);
        }
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
     * 左侧出队元素
     *
     * @param redisKey
     * @param key
     * @return
     */
    public <T> T lPopObj(RedisKey redisKey, String key, Class<T> returnType) {
        if (redisWorking) {
            String currentKey = generateKey(redisKey, key);
            return this.redisService.lPopObj(currentKey, returnType);
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
    public <T> T rPopObj(RedisKey redisKey, String key, Class<T> returnType) {
        if (redisWorking) {
            String currentKey = generateKey(redisKey, key);
            return this.redisService.rPopObj(currentKey, returnType);
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
    public <T> T lPeekObj(RedisKey redisKey, String key, Class<T> returnType) {
        if (redisWorking) {
            String currentKey = generateKey(redisKey, key);
            return this.redisService.lPeekObj(currentKey, returnType);
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
    public <T> T rPeekObj(RedisKey redisKey, String key, Class<T> returnType) {
        if (redisWorking) {
            String currentKey = generateKey(redisKey, key);
            return this.redisService.rPeekObj(currentKey, returnType);
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
    public <T> List<T> getList(RedisKey redisKey, String key, Class<T> returnType) {
        if (redisWorking) {
            String currentKey = generateKey(redisKey, key);
            return this.redisService.getList(currentKey, returnType);
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

    /**
     * 从队首出队，再从队尾入队
     *
     * @param redisKey
     * @param key
     * @return
     */
    public Object lPopAndRPush(RedisKey redisKey, String key) {
        if (redisWorking) {
            String currentKey = generateKey(redisKey, key);
            return this.redisService.lPopAndRightPush(currentKey);
        } else {
            return null;
        }
    }

    /**
     * 从A队列队首出队，将对应的元素，从B队列队尾入队
     *
     * @param redisKey
     * @param sourceKey
     * @param destinationKey
     * @return
     */
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
     * 从队首出队，再从队尾入队
     *
     * @param redisKey
     * @param key
     * @return
     */
    public <T> T lPopAndRPush(RedisKey redisKey, String key, Class<T> returnType) {
        if (redisWorking) {
            String currentKey = generateKey(redisKey, key);
            return this.redisService.lPopAndRightPush(currentKey, returnType);
        } else {
            return null;
        }
    }

    /**
     * 从A队列队首出队，将对应的元素，从B队列队尾入队
     *
     * @param redisKey
     * @param sourceKey
     * @param destinationKey
     * @return
     */
    public <T> T lPopAndRPushToAnother(RedisKey redisKey, String sourceKey, String destinationKey, Class<T> returnType) {
        if (redisWorking) {
            String currentSourceKey = generateKey(redisKey, sourceKey);
            String currentDestinationKey = generateKey(redisKey, destinationKey);
            return this.redisService.lPopAndRPushToAnother(currentSourceKey, currentDestinationKey, returnType);
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

}
