package com.song.redis;

import org.junit.Assert;
import redis.clients.jedis.Jedis;

public class SimpleLockExecutionThread extends Thread {
    private int i;
    private String host;
    private int port;
    private String password;

    public SimpleLockExecutionThread() {

    }

    public SimpleLockExecutionThread(String host, int port, String password, int i) {
        this.host = host;
        this.port = port;
        this.password = password;
        this.i = i;
    }

    public void run() {
        Jedis jedis = JedisUtils.getNewInstance(host, port, password);
        while (true) {
            String resourceName = "resourceLock";
            String uniqVal = JedisUtils.getRandomString();
            try {
                boolean success = JedisUtils.lock(jedis, resourceName, uniqVal, 10000);
                //do something,such as jdbc,read file etc.
                if (success) {
                    execution();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                JedisUtils.releaseLock(jedis, resourceName, uniqVal);
            }
            justSleep(1000);
        }
    }

    protected void execution() {
        final int k = i;
        for (int j = 0; j < 100; j++) {
            i++;
        }
        //我们以此验证对i的操作是安全的
        Assert.assertTrue(i == k + 100);
    }

    protected void justSleep(int i) {
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
