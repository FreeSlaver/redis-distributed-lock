package com.song.redis;

public class SimpleLock {
    private static int i = 0;

    public static void main(String[] args) {
        String host = "10.45.130.194";
        int port = 6379;
        String password = "redis@user";
        /**
         * 之所以生成2个实例，是因为一个jedis实例被2个线程使用，进行get(KEY)的时候，会发生
         * Exception in thread "Thread-1" Exception in thread "Thread-0" redis.clients.jedis.exceptions.JedisConnectionException: Unexpected end of stream.
         */
        Thread a = new SimpleLockExecutionThread(host, port, password, i);
        Thread b = new SimpleLockExecutionThread(host, port, password, i);

        a.start();
        b.start();
    }
}
