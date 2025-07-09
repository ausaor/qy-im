package xyz.qy.implatform.service.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import javax.annotation.PreDestroy;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * 线程池工具类
 */
@Slf4j
@Component
public class SystemExecutorService {
    /**
     * 系统CPU核数
     */
    private static final int CPU_SIZE = Runtime.getRuntime().availableProcessors();

    /**
     * 异步线程池队列容量
     */
    private static final int ASYNC_QUEUE_LENGTH = 5000;

    /**
     * 每个线程最多处理的任务数量
     */
    private static final int THREAD_TASK_NUM = 5;

    /**
     * CPU密集型任务所用线程池
     */
    private final ExecutorService executorForCpu;

    /**
     * IO密集型任务所用线程池
     */
    private final ExecutorService executorForIo;

    /**
     * 异步任务所用线程池
     */
    private final ExecutorService executorForAsync;

    public SystemExecutorService() {
        int threadNumForIo = CPU_SIZE * 10;
        executorForAsync = createExecutor("executorForAsync-", CPU_SIZE, ASYNC_QUEUE_LENGTH);
        executorForCpu = createExecutor("executorForCpu-", CPU_SIZE, CPU_SIZE * THREAD_TASK_NUM);
        executorForIo = createExecutor("executorForIo-", threadNumForIo, threadNumForIo * THREAD_TASK_NUM);
    }

    public ExecutorService getExecutorForCpu() {
        return executorForCpu;
    }

    public ExecutorService getExecutorForIo() {
        return executorForIo;
    }

    public ExecutorService getExecutorForAsync() {
        return executorForAsync;
    }

    /**
     * 异步执行任务，不返回结果
     *
     * @param runnableList 待执行的任务
     */
    public void runForAsync(List<Runnable> runnableList) {
        if (CollectionUtils.isEmpty(runnableList)) {
            return;
        }
        runnableList.forEach(this::runAsync);
    }

    /**
     * 异步执行任务，不返回结果
     *
     * @param runnable 待执行的任务
     */
    public void runAsync(Runnable runnable) {
        CompletableFuture.runAsync(runnable, getExecutorForAsync());
    }

    /**
     * CPU密集型任务异步执行，不返回结果
     *
     * @param runnableList 待执行的任务
     */
    public void runForCpu(List<Runnable> runnableList) {
        run(runnableList, getExecutorForCpu());
    }

    /**
     * IO密集型任务异步执行，不返回结果
     *
     * @param runnableList 待执行的任务
     */
    public void runForIo(List<Runnable> runnableList) {
        run(runnableList, getExecutorForIo());
    }

    private void run(List<Runnable> runnableList, ExecutorService executor) {
        if (CollectionUtils.isEmpty(runnableList)) {
            return;
        }
        long start = System.currentTimeMillis();

        List<CompletableFuture<Void>> futureList = runnableList.stream()
                .map(runnable -> CompletableFuture.runAsync(runnable, executor))
                .collect(Collectors.toList());
        CompletableFuture<Void> future = CompletableFuture.allOf(futureList.toArray(new CompletableFuture[0]));

        getResult(future);

        log.info("task done, threadCount={}, cost={}ms", runnableList.size(), System.currentTimeMillis() - start);
    }

    /**
     * CPU密集型任务异步执行，并等待结果返回
     *
     * @param dataList 待处理的数据
     * @param function 处理数据的功能函数
     * @param <T>      参数类型
     * @param <R>      返回类型
     * @return 结果
     */
    public <T, R> Map<T, R> supplyForCpu(List<T> dataList, Function<T, R> function) {
        return supply(dataList, function, getExecutorForCpu());
    }

    /**
     * IO密集型任务异步执行，并等待结果返回
     *
     * @param dataList 待处理的数据
     * @param function 处理数据的功能函数
     * @param <T>      参数类型
     * @param <R>      返回类型
     * @return 结果
     */
    public <T, R> Map<T, R> supplyForIo(List<T> dataList, Function<T, R> function) {
        return supply(dataList, function, getExecutorForIo());
    }

    private <T, R> Map<T, R> supply(List<T> dataList, Function<T, R> function, Executor executor) {
        if (CollectionUtils.isEmpty(dataList)) {
            return Collections.emptyMap();
        }
        long start = System.currentTimeMillis();
        List<Supplier<R>> supplierList = dataList.stream()
                .map(data -> getSupplier(data, function)).collect(Collectors.toList());

        List<R> resultList = supply(supplierList, executor);
        if (resultList.size() != dataList.size()) {
            return Collections.emptyMap();
        }

        Map<T, R> map = new HashMap<>();
        for (int i = 0; i < dataList.size(); i++) {
            map.put(dataList.get(i), resultList.get(i));
        }
        log.info("task done, threadCount={}, cost={}ms", dataList.size(), System.currentTimeMillis() - start);
        return map;
    }

    private <R> List<R> supply(List<Supplier<R>> supplierList, Executor executor) {
        List<CompletableFuture<R>> futures = supplierList.stream()
                .map(supply -> CompletableFuture.supplyAsync(supply, executor)).collect(Collectors.toList());

        CompletableFuture<Void> allFuture = CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
        CompletableFuture<List<R>> restFuture = allFuture
                .thenApply(f -> futures.stream().map(this::getResult).collect(Collectors.toList()));

        List<R> result = getResult(restFuture);
        result = result == null ? Collections.emptyList() : result;
        return result;
    }

    private <R> R getResult(CompletableFuture<R> future) {
        try {
            return future.get(50, TimeUnit.SECONDS);
        } catch (InterruptedException | ExecutionException | TimeoutException e) {
            e.printStackTrace();
            future.cancel(true);
            return null;
        }
    }

    private <T, R> Supplier<R> getSupplier(T data, Function<T, R> function) {
        return () -> function.apply(data);
    }

    private ExecutorService createExecutor(String namePrefix, int threadNum, int queueLength) {
        ThreadFactory threadFactory = new CustomizableThreadFactory(namePrefix);

        BlockingQueue<Runnable> queue = new LinkedBlockingQueue<>(queueLength);
        RejectedExecutionHandler callerRunsPolicy = new ThreadPoolExecutor.CallerRunsPolicy();
        return new ThreadPoolExecutor(
                threadNum,
                threadNum,
                0L,
                TimeUnit.SECONDS,
                queue,
                threadFactory,
                callerRunsPolicy);
    }

    @PreDestroy
    public void shutdown() {
        shutdownExecutor("executorForCpu", executorForCpu);
        shutdownExecutor("executorForIo", executorForIo);
        shutdownExecutor("executorForAsync", executorForAsync);
    }

    private void shutdownExecutor(String name, ExecutorService executor) {
        try {
            executor.shutdownNow();
        } catch (Exception e) {
            log.error("shutdown executor error, name:{}, error:{}", name, e.getMessage());
        }
        if (executor.isShutdown()) {
            log.info("shutdown {} success", name);
        } else {
            log.info("shutdown {} failure", name);
        }
    }
}
