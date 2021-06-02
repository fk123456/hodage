package com.foreign.Java并发编程基础;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;

/**
 * @author fangke
 * @Description:
 * @Package
 * @date: 2021/6/2 4:07 下午
 * <p>
 */

/**
 * 一个Java的main方法运行，其实是main线程和其它线程的运行
 */
public class MultiThread {
    public static void main(String[] args) {
        //获取Java线程管理的Bean
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        //仅获取线程和线程栈的信息
        ThreadInfo[] threadInfos = threadMXBean.dumpAllThreads(false, false);
        Arrays.stream(threadInfos).forEach(item -> System.out.println("Thread Id = " + item.getThreadId() + " Thread Name = " + item.getThreadName()));
        /**
         * Thread Id = 5 Thread Name = Monitor Ctrl-Break
         * Thread Id = 4 Thread Name = Signal Dispatcher
         * Thread Id = 3 Thread Name = Finalizer
         * Thread Id = 2 Thread Name = Reference Handler
         * Thread Id = 1 Thread Name = main
         */
    }
}
