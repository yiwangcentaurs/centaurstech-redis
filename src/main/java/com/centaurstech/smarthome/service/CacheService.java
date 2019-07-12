package com.centaurstech.smarthome.service;

import com.centaurstech.domain.cache.TimeBasedCache;
import com.centaurstech.smarthome.interfaces.RedisKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CacheService {
    private static final Logger logger = LoggerFactory.getLogger(CacheService.class);
    private RedisService redisService;
    private TimeBasedCache<Object> cacheContainer;
    private boolean redisWorking=true;
    private static Long DEFAULT_TIME_OUT=24*3600*1000L;//默认缓存为一天

    @Autowired
    public CacheService(RedisService redisService){
        this.redisService=redisService;
        try{
            this.redisService.setObj("testForConnection",100000L,"ttc");
        }catch (Exception e){
            this.redisWorking=false;
            cacheContainer=new TimeBasedCache<>(DEFAULT_TIME_OUT);
        }
    }

    private String generateKey(RedisKey table, String key){
        //全部以"类名:key"作为唯一键 ，所以key要保证唯一，类名相当于表名
        return new StringBuffer(table.getKey()).append(":").append(key).toString();
    }

    /**
     * 从reids或者内存获取对象
     * @param redisTable 给每个对象指定的key前缀，在RedisTable枚举定义，防止不同人使用冲突
     * @param key 对象唯一key
     * @return 返回key对应的对象
     */
    public Object getObj(RedisKey redisTable,String key){
        Object result=null;
        String currentKey=generateKey(redisTable,key);
        if(this.redisWorking){
            result=this.redisService.getObj(currentKey);
        }else{
            result=this.cacheContainer.get(currentKey);
        }
        return result;
    }

    /**
     * 参见setObj(redisTable, key, value, timeout)
     * @param redisTable
     * @param key
     * @param value
     */
    public void setObj(RedisKey redisTable, String key, Object value) {
        setObj(redisTable, key, value, null);
    }

    /**
     * 向redis或者内存放入对象
     * @param redisTable 给每个对象指定的key前缀，在RedisTable枚举定义，防止不同人使用冲突
     * @param key 对象唯一key
     * @param value 需要放入的对象
     * @param timeout 缓存时间，单位毫秒
     */
    public void setObj(RedisKey redisTable,String key,Object value,Long timeout){
        String currentKey=generateKey(redisTable,key);
        if(timeout==null){
            timeout=DEFAULT_TIME_OUT;
        }
        if(this.redisWorking){
            this.redisService.setObj(currentKey,timeout,value);
        }else{
            this.cacheContainer.put(currentKey,value,timeout);
        }
    }

    /**
     * 删除redis或者内存中对应key的对象
     * @param redisTable 给每个对象指定的key前缀，在RedisTable枚举定义，防止不同人使用冲突
     * @param key 对象唯一key
     */
    public void delKey(RedisKey redisTable,String key){
        String currentKey=generateKey(redisTable,key);
        if(this.redisWorking){
            this.redisService.deleteKey(currentKey);
        }else{
            this.cacheContainer.put(currentKey,null);
        }
    }

    /**
     * 判断redis或者内存中是否包含指定key
     * @param redisTable 给每个对象指定的key前缀，在RedisTable枚举定义，防止不同人使用冲突
     * @param key 对象唯一key
     * @return 存在true，不存在false
     */
    public boolean containKey(RedisKey redisTable,String key){
        String currentKey=generateKey(redisTable,key);
        boolean result=false;
        if(this.redisWorking){
            result=this.redisService.existsKey(currentKey);
        }else{
            result=this.cacheContainer.contains(key);
        }
        return result;
    }
}
