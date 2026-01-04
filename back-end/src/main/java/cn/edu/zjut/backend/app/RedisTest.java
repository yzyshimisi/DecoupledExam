package cn.edu.zjut.backend.app;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Map;

public class RedisTest {
    private static final JedisPool jedisPool;
    private Jedis jedis;
    private final Log log = LogFactory.getLog(RedisTest.class);

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

        jedisPool = new JedisPool(jedisPoolConfig,"x.x.x.x",6379,
                1000,"xxxx");

    }

    public void setUp(){
        //建立连接
        jedis = new Jedis("127.0.0.1",6379);

        //选择库
        jedis.select(0);
    }

    public void testString(){
        jedis.set("namePool","zhangsanPool");
        String value = jedis.get("namePool");
        log.info("value:"+value);
    }

    public void testHash(){
        jedis.hset("user:2","name","lisiPool");
        jedis.hset("user:2","age","21");
        Map<String,String> map = jedis.hgetAll("user:2");

        log.info("map:"+ map.toString());
    }

    public void tearDown(){
        if(jedis != null){
            jedis.close();
        }
    }

    public static void main(String[] args) {
        //建立连接
        Jedis jedis = new Jedis("127.0.0.1",6379);

        //选择库
        jedis.select(0);

        jedis.hset("user:2","name","lisiPool");
        jedis.hset("user:2","age","21");
        Map<String,String> map = jedis.hgetAll("user:2");

        System.out.println("map:"+ map.toString());

        if(jedis != null){
            jedis.close();
        }
    }
}
