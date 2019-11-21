# centaurstech-redis
封装redis工具，便于直接使用。当本地或服务器无redis时，会直接将数据放入内存中，不会报错。

## 使用方法
1. 加入依赖，引入该jar包。
2. 实现RedisKey接口，并实现其中的getKey方法，推荐用enum类型。
3. 在SpringBootApplication的启动类上面加注解@EnableCentaursRedis
4. 注入CacheService，直接使用CacheService中的方法。

## 工具扩展
可以自行继承RedisService或者CacheService
~~~java
@Service
public class RedisServiceV2 extends RedisService {
    @Autowired
    public RedisServiceV2(RedisTemplate<String, Object> redisTemplate, HashOperations<String, String, Object> hashOperations, ValueOperations<String, Object> valueOperations, ListOperations<String, Object> listOperations, SetOperations<String, Object> setOperations, ZSetOperations<String, Object> zSetOperations) {
        super(redisTemplate, hashOperations, valueOperations, listOperations, setOperations, zSetOperations);
    }
    public void test(){
        System.out.println("you need to do sth");
    }
}

@Service("cacheServiceV2")
public class CacheServiceV2 extends CacheService {
    public CacheServiceV2(RedisService redisService) {
        super(redisService);
        this.test();
    }

    public void test(){
        System.out.println("TEST　ＣａｃｈｅＳｅｒｖｉｃｅＶ２　哈哈哈哈！");
    }
}

public class SomeService{
    @Autowired
    @Qualifier("cacheServiceV2")
    CacheServiceV2 cacheService;
}

~~~
