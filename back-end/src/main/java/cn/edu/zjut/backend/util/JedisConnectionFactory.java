package cn.edu.zjut.backend.util;

import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

public class JedisConnectionFactory {
    private static final JedisPool jedisPool;

    static{
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();

        //最大连接
        jedisPoolConfig.setMaxTotal(10);
        //最大空闲连接
        jedisPoolConfig.setMaxIdle(10);
        //最小空闲连接
        jedisPoolConfig.setMinIdle(5);

        //设置最长等待时间
        jedisPoolConfig.setMaxWaitMillis(200);

        jedisPool = new JedisPool(jedisPoolConfig,"127.0.0.1",6379, 1000);

    }

    //获取jedis对象
    public static Jedis getJedis(){
        return jedisPool.getResource();
    }
}

