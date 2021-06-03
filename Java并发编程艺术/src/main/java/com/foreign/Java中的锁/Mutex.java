package com.foreign.Java中的锁;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author fangke
 * @Description:
 * @Package
 * @date: 2021/6/3 10:32 上午
 * <p>
 */
public class Mutex implements Lock {

    private static class Sync extends AbstractQueuedSynchronizer {
        //当状态为0的时候 获取锁
        @Override
        protected boolean tryAcquire(int arg) {
            if(compareAndSetState(1,0)) {
                setExclusiveOwnerThread(Thread.currentThread());
                return true;
            }
            return false;
        }

        //当状态为1的时候 释放锁
        @Override
        protected boolean tryRelease(int arg) {
            if(getState() == 0)
                throw new IllegalMonitorStateException();
            setExclusiveOwnerThread(null);
            setState(0);
            return true;
        }

        //是否处于独占状态
        @Override
        protected boolean isHeldExclusively() {
            return getState() == 1;
        }

        //ConditionObject内部包含一个等待队列
        Condition newCondition() { return new ConditionObject(); }
    }

    private Sync sync = new Sync();

    @Override
    public void lock() {
        sync.acquire(1);
    }

    @Override
    public void lockInterruptibly() throws InterruptedException {
        sync.acquireInterruptibly(1);
    }

    @Override
    public boolean tryLock() {
        return sync.tryAcquire(1);
    }

    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        return sync.tryAcquireNanos(1, unit.toNanos(time));
    }

    @Override
    public void unlock() {
        sync.release(1);
    }

    @Override
    public Condition newCondition() {
        return sync.newCondition();
    }
}
