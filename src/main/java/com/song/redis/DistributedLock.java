package com.song.redis;

/**
 * Created by 00013708 on 2017/11/14.
 */
public class DistributedLock {
    private static int i = 0;

    public static void main(String[] args) {
        String host = "10.45.130.194";
        int port = 6379;
        String password = "redis@user";

        Thread a = new DistributedLockExecutionThread(host, port, password, i);
        Thread b = new DistributedLockExecutionThread(host, port, password, i);

        a.start();
        b.start();
    }
}
