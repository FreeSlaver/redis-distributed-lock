package com.song.redis;

import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JedisUtils {

    public static Jedis getNewInstance(String host, int port, String password) {
        Jedis jedis = new Jedis(host, port);
        String authResult = jedis.auth(password);
        System.out.println("auth result:" + authResult);
        jedis.connect();
        jedis.select(8);
        return jedis;
    }

    /**
     * @param key
     * @param value
     * @param timeout milliseconds
     * @return
     */
    public static boolean lock(Jedis jedis, String key, String value, long timeout) {
        return jedis.setnx(key, value) == 1 ? jedis.pexpire(key, timeout) == 1 : false;
    }

    public static void releaseLock(Jedis jedis, String key, String value) {
        if (value.equals(jedis.get(key))) {
            jedis.del(key);
        }
    }

    public static String getRandomString() {
        return new StringBuilder().append(System.currentTimeMillis()).append(Thread.currentThread().getId()).toString();
    }

    public static List<Jedis> distributedLock(List<Jedis> jedisList, String key, String value, long timeout, long executionTime) {
        long t1 = System.currentTimeMillis();
        List<Jedis> acquiredLockJedis = new ArrayList<Jedis>();
        for (Jedis jedis : jedisList) {
            boolean lockResult = JedisUtils.lock(jedis, key, value, timeout);
            if (lockResult) {
                acquiredLockJedis.add(jedis);
            }
        }
        long t2 = System.currentTimeMillis();
        long acquireLocksExpireTime = t2 - t1;
        //要减去pc的时间差，还要减去一个获取锁的耗费时间，得到最终有效时间
        //有效事件必须大于任务执行完成的时间，才认为获取锁有效
        long validityTime = timeout - 500 - acquireLocksExpireTime;
        if (validityTime > executionTime && acquiredLockJedis.size() >= jedisList.size() / 2 + 1) {//获取锁成功
            return acquiredLockJedis;
        } else {
            releaseDistributedLock(acquiredLockJedis, key, value);
            return Collections.emptyList();
        }
    }

    /**
     * 其实不需要返回值，是自己设置的value就删除key，释放锁；不是自己设置的，就忽略掉
     *
     * @param jedisList
     * @param key
     * @param value
     */
    public static void releaseDistributedLock(List<Jedis> jedisList, String key, String value) {
        for (Jedis jedis : jedisList) {
            releaseLock(jedis, key, value);
        }
    }
}
