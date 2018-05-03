package com.potflesh.wenda.service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Transaction;
import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 如果Application类所在的包为：
 * io.github.gefangshuai.app
 * ，则只会扫描io.github.gefangshuai.app
 * 包及其所有子包，如果service或dao所在包不在io.github.gefangshuai.app
 * 及其子包下，则不会被扫描！
 */

@Service
public class RedisService implements InitializingBean{
    // 用于初始化变量
    // 相当于 dao 层
    private static final Logger logger= LoggerFactory.getLogger(RedisService.class);

    private JedisPool jedisPool;

    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            jedisPool = new JedisPool("redis://localhost:6379/9");
        }catch (Exception e){
            logger.error("初始化连接Rdies失败"+ e.getMessage());
        }


    }

    public long sadd(String key,String value){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            // 关注赞和踩的结果所以选用 set 作为存储结构
            return jedis.sadd(key,value);
        }catch (Exception e){
            logger.error("Rides发送异常"+e.getMessage());
        }finally {
            if(jedis != null)
                jedis.close();
        }
        return 0;
    }

    public long srem(String key,String value){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            // 关注赞和踩的结果所以选用 set 作为存储结构
            return jedis.srem(key,value);
        }catch (Exception e){
            logger.error("Rides移除异常"+e.getMessage());
        }finally {
            if(jedis != null)
                jedis.close();
        }
        return 0;
    }

    // 返回多少人
    public long scard(String key){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            // 关注赞和踩的结果所以选用 set 作为存储结构
            return jedis.scard(key);
        }catch (Exception e){
            logger.error("Rides移除异常"+e.getMessage());
        }finally {
            if(jedis != null)
                jedis.close();
        }
        return 0;
    }

    // 这个key是否在集合里存在
    public boolean sismember(String key,String value){
        Jedis jedis = null;
        try{
            jedis = jedisPool.getResource();
            // 关注赞和踩的结果所以选用 set 作为存储结构
            return jedis.sismember(key,value);
        }catch (Exception e){
            logger.error("Rides移除异常"+e.getMessage());
        }finally {
            if(jedis != null)
                jedis.close();
        }
        return false;
    }

    // 在 list 的尾部取走一个元素
    public List<String> brpop(int timeout, String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.brpop(timeout, key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    // 在 list 的头插入
    public long lpush(String key, String value) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.lpush(key, value);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    public long lremfeed(String timelineKey, int id) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.lrem(timelineKey,
                    0,String.valueOf(id));
          //  return jedis.lrem(timelineKey,0,id);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }


    public List<String> lrange(String key, int start, int end) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.lrange(key, start, end);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    public Jedis getJedis() {
        return jedisPool.getResource();
    }

    /**
     *  Redis 的事务相关 开启多个事件的执行
     * @param jedis
     * @return
     */

    public Transaction multi(Jedis jedis) {
        try {
            return jedis.multi();
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
        }
        return null;
    }

    /**
     * 事务的执行
     * @param tx
     * @param jedis
     * @return
     */

    public List<Object> exec(Transaction tx, Jedis jedis) {
        try {
            return tx.exec();
        } catch (Exception e) {
            logger.error("事务执行发生异常" + e.getMessage());
            tx.discard();
        } finally {
            if (tx != null) {
                try {
                    tx.close();
                } catch (IOException e) {
                    logger.error("事务执行发生异常" + e.getMessage());
                }
            }

            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    /**
     * 得到相应类型key的所有值
     * @param key
     * @param start
     * @param end
     * @return
     */
    public Set<String> zrange(String key, int start, int end) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zrange(key, start, end);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    /**
     * 注意这里取的是反向的因为按照时间排序，最新的在最后面
     * @param key
     * @param start
     * @param end
     * @return
     */

    public Set<String> zrevrange(String key, int start, int end) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zrevrange(key, start, end);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    /**
     * 得到某个 key 的集合的总数量
     * @param key
     * @return
     */

    public long zcard(String key) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zcard(key);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return 0;
    }

    // 如果这个 key 在集合中的 key 为空，那么证明这个集合中没有这个 key
    public Double zscore(String key, String member) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.zscore(key, member);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

    /**
     * 取出输入 key 值相似的一部分
     * @param partOfKey
     * @return
     */
    public Set<String> getKeys(String partOfKey) {
        Jedis jedis = null;
        try {
            jedis = jedisPool.getResource();
            return jedis.keys(partOfKey);
        } catch (Exception e) {
            logger.error("发生异常" + e.getMessage());
        } finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return null;
    }

}
