# centaurstech-redis
封装redis工具，便于直接使用。当本地或服务器无redis时，会直接将数据放入内存中，不会报错。

## 使用方法
1. 加入依赖，引入该jar包。
2. 实现RedisKey接口，并实现其中的getKey方法，推荐用enum类型。
3. 注入CacheService，直接使用CacheService中的方法。

## 工具扩展
可以自行继承RedisService或者CacheService