package com.foreign.Java并发编程基础;

import java.util.concurrent.TimeUnit;

/**
 * @author fangke
 * @Description:
 * @Package
 * @date: 2021/6/2 4:38 下午
 * <p>
 */
public class MyThreadLocal {

    static ThreadLocal<Long> THREAD_LOCAL = ThreadLocal.withInitial(System::currentTimeMillis);

    private static final long end() {
        return System.currentTimeMillis() - THREAD_LOCAL.get();
    }

    private static final long begin() {
        return THREAD_LOCAL.get();
    }

    public static void main(String[] args) {
        long begin = MyThreadLocal.begin();
        try {
            TimeUnit.MILLISECONDS.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println(end());
    }
}
