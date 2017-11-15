package com.song.redis;

import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

public class DistributedLockExecutionThread extends SimpleLockExecutionThread {
    private String host;
    private int port;
    private String password;

    private int i;

    public DistributedLockExecutionThread(String host, int port, String password, int i) {
        super(host,port,password,i);
        this.host = host;
        this.port = port;
        this.password = password;
        this.i = i;
    }

    @Override
    public void run() {
        List<Jedis> jedisList = new ArrayList<Jedis>();
        for (int i = 0; i < 5; i++) {
            Jedis jedis = JedisUtils.getNewInstance(host, port + i, password);
            jedisList.add(jedis);
        }
        while (true) {
            String resourceName = "resourceLock";
            String uniqVal = JedisUtils.getRandomString();

            long timeout = 10000;//释放锁时间
            long executionTime = 8000;
            List<Jedis> acquiredLockJedis = JedisUtils.distributedLock(jedisList, resourceName, uniqVal, timeout,executionTime);
            if (acquiredLockJedis == null || acquiredLockJedis.isEmpty()) {
                //retry();  //获取锁失败进行尝试。
            } else {
                //do something
                super.execution();
            }
            justSleep(1000);
        }
    }
}
