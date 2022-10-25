package com.ding.schedule;

import java.util.concurrent.*;

import com.ding.utils.TimeUtils;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.springframework.stereotype.Service;


/**
 * @author yanou
 * @date 2022年10月24日 4:19 下午
 */
@Service
public class ScheduleService {

    private final ScheduledThreadPoolExecutor executor;

    public ScheduleService() {
        executor = new ScheduledThreadPoolExecutor(8,
                new ThreadFactoryBuilder().setNameFormat("default-scheduled-executor-%d").build());
    }

    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return executor.schedule(wrap(command), delay, unit);
    }


    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return executor.schedule(wrap(callable), delay, unit);
    }


    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return executor.scheduleAtFixedRate(wrap(command), initialDelay, period, unit);
    }


    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return executor.scheduleWithFixedDelay(wrap(command), initialDelay, delay, unit);
    }


    public ScheduledFuture<?> scheduleAtHIS(Runnable command, int hour, int minute, int second) {
        long nowMills = TimeUtils.getBizMillis();
        long nowTs = TimeUtils.getBizTs();
        long delay = 0L;
        long todayMills = TimeUtils.getGivenHourMillisFromNow(nowMills, 0, hour) +
                (long) minute * TimeUtils.MINUTE_MILLS +
                (long) second * TimeUtils.SEC_MILLS;
        long todayTs = todayMills / TimeUtils.SEC_MILLS;
        //今天的刷新时间还没到，设置delay为差值
        if (todayTs > nowTs) {
            delay = todayTs - nowTs;
        }
        //今天刷新时间已过，需算出明天的刷新时间与当前时间做差，作为delay
        else {
            delay = todayTs + TimeUtils.DAY_SECONDS - nowTs;
        }
        return executor.scheduleAtFixedRate(wrap(command), delay, TimeUtils.DAY_SECONDS, TimeUnit.SECONDS);
    }


    public ScheduledExecutorService executor() {
        return executor;
    }

    private Runnable wrap(Runnable runnable) {
        return () -> {
            try {
                runnable.run();
            } catch (Exception e) {
                throw e;
            }
        };
    }

    private <T> Callable<T> wrap(Callable<T> callable) {
        return () -> {
            try {
                return callable.call();
            } catch (Exception e) {
                throw e;
            }
        };
    }
}