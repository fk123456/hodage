package com.foreign.Executors;

import jodd.util.concurrent.ThreadFactoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

/**
 * @author fangke
 * @Description:
 * @Package
 * @date: 2021/6/1 9:40 上午
 * <p>
 */
@RestController
public class MyExecutors {

    Logger logger = LoggerFactory.getLogger(MyExecutors.class);

    public static void main(String[] args) {
        //这种方法创建线程池，内部默认创建一个Integer.MAX_VALUE大小的LinkedBlockingQueue
        ThreadPoolExecutor poolExecutor = (ThreadPoolExecutor) Executors.newFixedThreadPool(1);

        //这种方法创建线程池，内部默认创建一个没有存储空间的阻塞队列SynchronousQueue
        ThreadPoolExecutor poolExecutor1 = (ThreadPoolExecutor) Executors.newCachedThreadPool();
    }

    /**
     * 自己定义线程池
     *
     * rejected from java.util.concurrent.ThreadPoolExecutor@1fb7d6f1
     * [Running, pool size = 5, active threads = 5, queued tasks = 10, completed tasks = 2]
     *
     * 总结：
     * 1）不会初始化corePoolSize（一般是CPU核数/CPU核数*2），有任务来了才会创建核心线程（可以调用preStartAllCoreThreads的方法来启动核心线程）
     * 2）当核心线程满了之后，不会立刻扩充线程池而是把任务放到队列中
     * 3）当工作队列满了之后扩充线程池，一直到线程个数达到maximumPoolSize为止
     * 4）如果队列已满且线程数达到最大个数时，有任务进来执行拒绝粗略
     * 5）当线程数大于核心数时，线程等待keepAliveTime还没有人任务进来，收缩线程到核心线程数（传入true让allowCoreThreadTimeout方法来收缩核心线程）
     */
    @GetMapping("/myExecutorsWrong")
    public int wrong() {
        //定义一个计数器
        AtomicInteger atomicInteger = new AtomicInteger();

        //自定义线程池
        ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(2, 5, 5,
                TimeUnit.SECONDS, new ArrayBlockingQueue<>(10),
                //给线程命名，使用Joda框架提供
                new ThreadFactoryBuilder().setNameFormat("thread-pool-%d").get(),
                new ThreadPoolExecutor.AbortPolicy());

        printStats(poolExecutor);
        //每隔1秒提交一次，一共提交20次
        IntStream.rangeClosed(1,20).forEach(i -> {
            try {
                TimeUnit.SECONDS.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            int id = atomicInteger.incrementAndGet();
            try {
                poolExecutor.submit(() -> {
                    logger.info("{} started", id);
                    try {
                        //休眠10秒
                        TimeUnit.SECONDS.sleep(10);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    logger.info("{} end", id);
                });
            }catch (Exception e) {
                //出现异常，打印提交信息，减一
                logger.error("Error submit task {}", id, e);
                atomicInteger.decrementAndGet();
            }
        });

        try {
            TimeUnit.SECONDS.sleep(60);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return atomicInteger.get();
    }

    private void printStats(ThreadPoolExecutor executor) {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            logger.info("=====================");
            logger.info("Pool size : {}", executor.getCorePoolSize());
            logger.info("Active Thread : {}", executor.getActiveCount());
            logger.info("Number of Completed: {}", executor.getCompletedTaskCount());
            logger.info("Number of Tasks in Queue: {}", executor.getQueue().size());
        }, 0, 1, TimeUnit.SECONDS);
    }
}
