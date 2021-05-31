package com.foreign.ThreadLocal;

/**
 * @author fangke
 * @Description:
 * @Package
 * @date: 2021/5/31 2:28 下午
 * <p>
 */

import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 在ThreadLocal中保存用户信息，并且从中获取
 */
@RestController
@RequestMapping("/threadlocal")
public class Wrong {
    private ThreadLocal<Integer> threadLocal = ThreadLocal.withInitial(() -> null);


    /**
     * 程序运行在tomcat中，执行程序的线程是tomcat工作线程，而Tomcat的工作线程是基于线程池的
     * 一旦重用线程池，就很可能导致ThreadLocal中的信息是上一个线程留下来的
     * <p>
     * server:
     * tomcat:
     * # 设置tomcat线程数
     * max-threads: 200
     * # 设置tomcat连接数
     * max-connections: 300
     */
    @GetMapping("/wrong")
    public Map<Integer, String> wrong(@RequestParam("userId") Integer userId) {

        String before = Thread.currentThread().getName() + " : " + threadLocal.get();

        threadLocal.set(userId);

        String after = Thread.currentThread().getName() + " : " + threadLocal.get();

        Map<Integer, String> result = new HashMap();
        result.put(1, before);
        result.put(2, after);
        return result;
    }

    @GetMapping("/right")
    public Map<Integer, String> right(@RequestParam("userId") Integer userId) {
        String before = Thread.currentThread().getName() + " : " + threadLocal.get();

        threadLocal.set(userId);

        try {
            String after = Thread.currentThread().getName() + " : " + threadLocal.get();

            Map<Integer, String> result = new HashMap();
            result.put(1, before);
            result.put(2, after);
            return result;
        } finally {
            //每次都清空threadlocal中的信息
            threadLocal.remove();
        }
    }
}
