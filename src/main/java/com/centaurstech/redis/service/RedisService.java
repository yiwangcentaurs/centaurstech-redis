package com.centaurstech.redis.service;

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
@Qualifier("redisService")
@ConditionalOnMissingBean(value = {RedisService.class})
public class RedisService {
    public RedisTemplate<String, Object> redisTemplate;
    public HashOperations<String, String, Object> hashOperations;
    public ValueOperations<String, Object> valueOperations;
    public ListOperations<String, Object> listOperations;
    public SetOperations<String, Object> setOperations;
    public ZSetOperations<String, Object> zSetOperations;

    @Autowired
    public RedisService(@Qualifier("redisTemplate") RedisTemplate<String, Object> redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
        this.listOperations = redisTemplate.opsForList();
        this.setOperations = redisTemplate.opsForSet();
        this.zSetOperations = redisTemplate.opsForZSet();
        this.valueOperations = redisTemplate.opsForValue();
    }
    /**
     * 默认过期时长，单位：秒
     */
    //public static final long DEFAULT_EXPIRE = 60 * 60 * 24;

    /**
     * 不设置过期时长
     */
    public static final long NOT_EXPIRE = -1;


    /**
     * 普通缓存放入
     *
     * @param key   键
     * @param value 值
     * @return true成功 false失败
     */

    public boolean set(String key, Object value) {

        try {

            redisTemplate.opsForValue().set(key, value);

            return true;

        } catch (Exception e) {

            e.printStackTrace();

            return false;

        }


    }


    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, Object value, long time) {

        try {

            if (time > 0) {

                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);

            } else {

                set(key, value);

            }

            return true;

        } catch (Exception e) {

            e.printStackTrace();

            return false;

        }

    }

    public boolean existsKey(String key) {
        return redisTemplate.hasKey(key);
    }

    /**
     * 重名名key，如果newKey已经存在，则newKey的原值被覆盖
     *
     * @param oldKey
     * @param newKey
     */
    public void renameKey(String oldKey, String newKey) {
        redisTemplate.rename(oldKey, newKey);
    }

    /**
     * newKey不存在时才重命名
     *
     * @param oldKey
     * @param newKey
     * @return 修改成功返回true
     */
    public boolean renameKeyNotExist(String oldKey, String newKey) {
        return redisTemplate.renameIfAbsent(oldKey, newKey);
    }

    /**
     * 删除key
     *
     * @param key
     */
    public void deleteKey(String key) {
        redisTemplate.delete(key);
    }

    /**
     * 删除多个key
     *
     * @param keys
     */
    public void deleteKey(String... keys) {
        Set<String> kSet = Stream.of(keys).map(k -> k).collect(Collectors.toSet());
        redisTemplate.delete(kSet);
    }

    /**
     * 删除Key的集合
     *
     * @param pattern
     * @return
     */
    public Set<String> findKeys(String pattern) {
        return redisTemplate.keys(pattern);
    }

    /**
     * 删除Key的集合
     *
     * @param keys
     */
    public Long deleteKey(Collection<String> keys) {
        Set<String> kSet = keys.stream().map(k -> k).collect(Collectors.toSet());
        return redisTemplate.delete(kSet);
    }

    /**
     * 设置key的生命周期
     *
     * @param key
     * @param time
     * @param timeUnit
     */
    public void expireKey(String key, long time, TimeUnit timeUnit) {
        redisTemplate.expire(key, time, timeUnit);
    }

    /**
     * 指定key在指定的日期过期
     *
     * @param key
     * @param date
     */
    public void expireKeyAt(String key, Date date) {
        redisTemplate.expireAt(key, date);
    }

    /**
     * 查询key的生命周期
     *
     * @param key
     * @param timeUnit
     * @return
     */
    public long getKeyExpire(String key, TimeUnit timeUnit) {
        return redisTemplate.getExpire(key, timeUnit);
    }

    /**
     * 将key设置为永久有效
     *
     * @param key
     */
    public void persistKey(String key) {
        redisTemplate.persist(key);
    }

    public Object getObj(String key) {
        return this.valueOperations.get(key);
    }

    /**
     * 获取对象
     * 类型转换由内部做，外部不用强制转换
     *
     * @param key
     * @param returnType
     * @param <T>
     * @return
     */
    public <T> T getObj(String key, Class<T> returnType) {
        return (T) this.valueOperations.get(key);
    }

    public void setObj(String key, Object value) {
        this.valueOperations.set(key, value);
    }

    public void setObj(String key, long timeout, Object value) {
        this.valueOperations.set(key, value, timeout, TimeUnit.MILLISECONDS);
    }

    /**
     * 从队列队尾入队
     *
     * @param key
     * @param value
     */
    public void rPushObj(String key, Object value) {
        this.listOperations.rightPush(key, value);
    }

    /**
     * 从队列队首入队
     *
     * @param key
     * @param value
     */
    public void lPushObj(String key, Object value) {
        this.listOperations.leftPush(key, value);
    }

    /**
     * 从队列队首出队
     *
     * @param key
     * @return
     */
    public Object lPopObj(String key) {
        return this.listOperations.leftPop(key);
    }

    /**
     * 从队列队首出队
     * 类型转换由内部做，外部不用强制转换
     *
     * @param key
     * @param returnType
     * @param <T>
     * @return
     */
    public <T> T lPopObj(String key, Class<T> returnType) {
        return (T) this.listOperations.leftPop(key);
    }

    public Object rPopObj(String key) {
        return this.listOperations.rightPop(key);
    }

    /**
     * 从队列队尾出队
     * 类型转换由内部做，外部不用强制转换
     *
     * @param key
     * @param returnType
     * @param <T>
     * @return
     */
    public <T> T rPopObj(String key, Class<T> returnType) {
        return (T) this.listOperations.rightPop(key);
    }

    /**
     * 获取队列队首元素（不移除）
     *
     * @param key
     * @return
     */
    public Object lPeekObj(String key) {
        return this.listOperations.index(key, 0L);
    }

    /**
     * 获取队列队首元素（不移除）
     * 类型转换由内部做，外部不用强制转换
     *
     * @param key
     * @param returnType
     * @param <T>
     * @return
     */
    public <T> T lPeekObj(String key, Class<T> returnType) {
        return (T) this.listOperations.index(key, 0L);
    }

    /**
     * 获取队列队尾元素（不移除）
     *
     * @param key
     * @return
     */
    public Object rPeekObj(String key) {
        Long size = this.listOperations.size(key);
        return this.listOperations.index(key, size - 1);
    }

    /**
     * 获取队列队尾元素（不移除）
     * 类型转换由内部做，外部不用强制转换
     *
     * @param key
     * @return
     */
    public <T> T rPeekObj(String key, Class<T> returnType) {
        Long size = this.listOperations.size(key);
        return (T) this.listOperations.index(key, size - 1);
    }

    public List<Object> getList(String key) {
        Long size = this.listOperations.size(key);
        if (size > 0) {
            return this.listOperations.range(key, 0, size);
        } else {
            return null;
        }
    }

    /**
     * 获取队列中所有元素
     * 类型转换由内部做，外部不用强制转换
     *
     * @param key
     * @return
     */
    public <T> List<T> getList(String key, Class<T> returnType) {
        Long size = this.listOperations.size(key);
        if (size > 0) {
            return (List<T>) this.listOperations.range(key, 0, size);
        } else {
            return null;
        }
    }

    public Long listSize(String key) {
        return this.listOperations.size(key);
    }

    public void setList(String key, List<Object> objs) {
        this.listOperations.rightPushAll(key, objs);
    }

    /**
     * 从队首出队，再从队尾入队
     *
     * @param key
     * @return
     */
    public Object lPopAndRightPush(String key) {
        Object obj = lPopObj(key);
        if (obj != null) {
            rPushObj(key, obj);
        }
        return obj;
    }

    /**
     * 从A队列队首出队，将对应的元素，从B队列队尾入队
     *
     * @param sourceKey      原队列
     * @param destinationKey 目标队列
     * @return
     */
    public Object lPopAndRPushToAnother(String sourceKey, String destinationKey) {
        Object obj = lPopObj(sourceKey);
        if (obj != null) {
            rPushObj(destinationKey, obj);
        }
        return obj;
    }

    /**
     * 从队首出队，再从队尾入队
     *
     * @param key
     * @return
     */
    public <T> T lPopAndRightPush(String key, Class<T> returnType) {
        Object obj = lPopObj(key);
        if (obj != null) {
            rPushObj(key, obj);
        }
        return (T) obj;
    }

    /**
     * 从A队列队首出队，将对应的元素，从B队列队尾入队
     *
     * @param sourceKey      原队列
     * @param destinationKey 目标队列
     * @return
     */
    public <T> T lPopAndRPushToAnother(String sourceKey, String destinationKey, Class<T> returnType) {
        Object obj = lPopObj(sourceKey);
        if (obj != null) {
            rPushObj(destinationKey, obj);
        }
        return (T) obj;
    }

    public Long removeFromList(String key, long count, Object obj) {
        return this.listOperations.remove(key, count, obj);
    }

    public boolean listContainObj(String key, Object obj) {
        List<Object> objs = getList(key);
        if (CollectionUtils.isEmpty(objs)) {
            return false;
        } else {
            return objs.contains(obj);
        }
    }

}
