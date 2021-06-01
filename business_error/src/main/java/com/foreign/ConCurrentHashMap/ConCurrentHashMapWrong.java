package com.foreign.ConCurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.LongStream;

/**
 * @author fangke
 * @Description:
 * @Package
 * @date: 2021/5/31 3:04 下午
 * <p>
 */
@RestController
public class ConCurrentHashMapWrong {

    Logger logger = LoggerFactory.getLogger(ConCurrentHashMapWrong.class);

    private static int THREAD_COUNT = 10;

    private static int ITEM_COUNT = 1000;

    private static int TOTAL_COUNT = 100000000;

    //帮助方法，获取指定元素数量的ConcurrentHashMap
    private ConcurrentHashMap<String, Long> getData(int count) {
        return LongStream.rangeClosed(1, count)
                .boxed()
                .collect(Collectors.toConcurrentMap(
                        i -> UUID.randomUUID().toString(),
                        j -> 1L,
                        (o1, o2) -> o1,
                        ConcurrentHashMap::new));
    }

    /**
     * ConcurrentHashMap就像一个大篮子，无法确保10个线程看到这个菜篮子里的数据一致性
     */
    @GetMapping("/conCurrentHashMapWrong")
    public void conCurrentHashMapWrong() {
        ConcurrentHashMap<String, Long> data = getData(ITEM_COUNT - 100);
        //初始化900个元素
        logger.error("init size() = " + data.size());

        //初始化10个线程
        ForkJoinPool forkJoinPool = new ForkJoinPool(THREAD_COUNT);
        forkJoinPool.execute(() -> IntStream.rangeClosed(1, 10).parallel().forEach(item -> {
            //还需要多少个ConCurrentHashMap
            int gap = ITEM_COUNT - data.size();
            logger.error("gap size() = " + gap);
            //补充元素
            data.putAll(getData(gap));
        }));
        //等待所有任务完成
        forkJoinPool.shutdown();
        try {
            forkJoinPool.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.error("最终的元素个数 = " + data.size());
    }

    @GetMapping("/conCurrentHashMapRight")
    public void conCurrentHashMapRight() {
        ConcurrentHashMap<String, Long> data = getData(ITEM_COUNT - 100);
        //初始化900个元素
        logger.error("init size() = " + data.size());

        //初始化10个线程
        ForkJoinPool forkJoinPool = new ForkJoinPool(THREAD_COUNT);
        forkJoinPool.execute(() -> IntStream.rangeClosed(1, 10).parallel().forEach(item -> {
            //这里采用了加锁的方式，并不能充分发挥并发工具的威力
            synchronized (data) {
                //还需要多少个ConCurrentHashMap
                int gap = ITEM_COUNT - data.size();
                logger.error("gap size() = " + gap);
                //补充元素
                data.putAll(getData(gap));
            }
        }));
        //等待所有任务完成
        forkJoinPool.shutdown();
        try {
            forkJoinPool.awaitTermination(1, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        logger.error("最终的元素个数 = " + data.size());
    }

    @GetMapping("/conCurrentHashMapGood")
    public Map<String, Long> conCurrentHashMapGood() throws InterruptedException {
        ConcurrentHashMap<String, LongAdder> concurrentHashMap = new ConcurrentHashMap(ITEM_COUNT);
        ForkJoinPool forkJoinPool = new ForkJoinPool(THREAD_COUNT);
        forkJoinPool.execute(() -> IntStream.rangeClosed(1, TOTAL_COUNT).parallel().forEach(item -> {
            String key = "item" + ThreadLocalRandom.current().nextInt(THREAD_COUNT);
            //此方法采用加锁的方式
//            synchronized (concurrentHashMap) {
//                if (concurrentHashMap.contains(key)) {
//                    concurrentHashMap.put(key, concurrentHashMap.get(key) + 1);
//                } else {
//                    concurrentHashMap.put(key, 1L);
//                }
//            }
            //使用原子性的方法computeIfAbsent来做复合操作
            //LongAddr也是一个线程安全的累加器
            //虚拟机层面使用的CAS操作来保证写入数据的原子性
            concurrentHashMap.computeIfAbsent(key, k -> new LongAdder()).increment();
        }));
        forkJoinPool.shutdown();
        forkJoinPool.awaitTermination(1, TimeUnit.HOURS);
        return concurrentHashMap.entrySet().stream()
                .collect(Collectors.toMap(
                        e -> e.getKey(),
                        e -> e.getValue().longValue()
                ));
    }
}
