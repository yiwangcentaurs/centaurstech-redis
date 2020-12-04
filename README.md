# centaurstech-redis
封装redis工具，便于直接使用。当本地或服务器无redis时，会直接将数据放入内存中，不会报错。

## 使用方法
1. 加入依赖，引入该jar包。
2. 实现RedisKey接口，并实现其中的getKey方法，推荐用enum类型。
3. 在SpringBootApplication的启动类上面加注解@EnableCentaursRedis
4. 使用方法（二选一即可） 
4.1 注入CacheService，直接使用CacheService中的方法。目前CacheService主要供bot后台使用，
4.2 注入CacheServiceV2，直接使用CacheServiceV2中的方法。目前除了除了bot后台均使用V2

## 工具扩展
可以自行继承RedisService(或V2)或者CacheService(或V2)
~~~java
@Service
public class RedisServiceV2 extends RedisService {
    @Autowired
    public RedisServiceV2(RedisTemplate<String, Object> redisTemplate) {
        super(redisTemplate);
    }
    public void test(){
        System.out.println("you need to do sth");
    }
}

@Service("cacheServiceV3")
public class CacheServiceV3 extends CacheService {
    public CacheServiceV3(RedisService redisService) {
        super(redisService);
        this.test();
    }

    public void test(){
        System.out.println("TEST　ＣａｃｈｅＳｅｒｖｉｃｅＶ２　哈哈哈哈！");
    }
}

public class SomeService{
    @Autowired
    @Qualifier("cacheServiceVx")
    CacheServiceV3 cacheService;
}

~~~
