package io.coderf.arklab.core.utils

import android.os.Handler
import android.os.Looper
import java.util.concurrent.ArrayBlockingQueue
import java.util.concurrent.Executor
import java.util.concurrent.ExecutorService
import java.util.concurrent.LinkedBlockingQueue
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * 框架级 Runnable 执行器门面（静态工具，非 Hilt / Gateway）。
 *
 * 选型：
 * - [io]：短时可并发 IO（上传等）；SynchronousQueue，忙则扩线程
 * - [bounded]：可排队后台任务（画廊下载等）；约 3 核心线程 + 队列，队列满后再扩
 * - [serial]：必须顺序的副作用（写日志 / 写文件）
 * - [cpu]：压缩等 CPU 密集投递
 * - [main]：回主线程
 *
 * 网络 / Room 等可挂起路径请继续用协程 [kotlinx.coroutines.Dispatchers.IO]，不要叠本门面。
 *
 * @author fz
 * @since 1.2.2
 */
object CoreExecutors {
    private const val TAG = "CoreExecutors"

    private val cpuCount: Int = Runtime.getRuntime().availableProcessors().coerceAtLeast(1)

    /** 短时可并发 IO（上传等）；SynchronousQueue，忙则扩至约 CPU×2 */
    private val ioPool: ThreadPoolExecutor by lazy { createIoPool() }
    /** 可排队后台任务；约 3 核心线程 + 队列 128，队列满后再扩 */
    private val boundedPool: ThreadPoolExecutor by lazy { createBoundedPool() }
    /** 单线程有序执行（写日志 / 写文件等副作用） */
    private val serialPool: ThreadPoolExecutor by lazy { createSerialPool() }
    /** CPU 密集任务投递（压缩等），线程数 ≈ CPU 核数 */
    private val cpuPool: ThreadPoolExecutor by lazy { createCpuPool() }
    /** 投递到主线程（Handler MainLooper） */
    private val mainExecutor: Executor by lazy {
        val handler = Handler(Looper.getMainLooper())
        Executor { command -> handler.post(command) }
    }

    /** 短时并发 IO。Java: `CoreExecutors.io().execute(r)` */
    @JvmStatic
    fun io(): ExecutorService = ioPool

    /** 可排队后台任务（队列优先）。 */
    @JvmStatic
    fun bounded(): ExecutorService = boundedPool

    /** 单线程有序执行。 */
    @JvmStatic
    fun serial(): ExecutorService = serialPool

    /** CPU 密集任务投递。 */
    @JvmStatic
    fun cpu(): ExecutorService = cpuPool

    /** 主线程。 */
    @JvmStatic
    fun main(): Executor = mainExecutor

    /**
     * IO 池：core=1，max=max(2, cpu×2)，SynchronousQueue（不排队，忙就扩线程）。
     * keepAlive=10s；超额 CallerRuns，避免 Abort 打崩业务。
     */
    private fun createIoPool(): ThreadPoolExecutor {
        val maximum = maxOf(2, cpuCount * 2)
        val core = 1
        return ThreadPoolExecutor(
            core,
            maximum,
            10L,
            TimeUnit.SECONDS,
            SynchronousQueue(),
            namedFactory("ark-io-"),
            loggingCallerRuns("io"),
        )
    }

    /**
     * 有界池：core=min(3, max)，max=max(3, cpu×2)，ArrayBlockingQueue(128)。
     * 队列优先（队列未满不扩超 core）；满后再扩到 max；keepAlive=5s。
     * 保证 max≥core，避免单核机 core>max 构造失败。
     */
    private fun createBoundedPool(): ThreadPoolExecutor {
        val maximum = maxOf(3, cpuCount * 2)
        val core = minOf(3, maximum)
        return ThreadPoolExecutor(
            core,
            maximum,
            5L,
            TimeUnit.SECONDS,
            ArrayBlockingQueue(128),
            namedFactory("ark-bound-"),
            loggingCallerRuns("bounded"),
        )
    }

    /**
     * 串行池：单线程 + 无界 LinkedBlockingQueue，保证任务按提交顺序执行。
     * 允许核心线程超时回收（空闲 30s），避免长期无任务时占住线程。
     */
    private fun createSerialPool(): ThreadPoolExecutor {
        return ThreadPoolExecutor(
            1,
            1,
            30L,
            TimeUnit.SECONDS,
            LinkedBlockingQueue(),
            namedFactory("ark-serial-"),
            loggingCallerRuns("serial"),
        ).also { it.allowCoreThreadTimeOut(true) }
    }

    /**
     * CPU 池：固定 n=cpu 线程，ArrayBlockingQueue(64) 背压。
     * 适合压缩等算力任务；队列满则 CallerRuns；空闲 30s 可回收核心线程。
     */
    private fun createCpuPool(): ThreadPoolExecutor {
        val n = cpuCount
        return ThreadPoolExecutor(
            n,
            n,
            30L,
            TimeUnit.SECONDS,
            ArrayBlockingQueue(64),
            namedFactory("ark-cpu-"),
            loggingCallerRuns("cpu"),
        ).also { it.allowCoreThreadTimeOut(true) }
    }

    /** 具名非 daemon 线程工厂，便于 Logcat 按 `ark-io-` / `ark-bound-` 等前缀过滤。 */
    private fun namedFactory(prefix: String): ThreadFactory {
        val count = AtomicInteger(1)
        return ThreadFactory { runnable ->
            Thread(runnable, prefix + count.getAndIncrement()).apply {
                isDaemon = false
            }
        }
    }

    /**
     * 超额策略：打警告日志后在调用线程执行（CallerRuns），
     * 提供背压且不抛 RejectedExecutionException。
     */
    private fun loggingCallerRuns(slot: String): ThreadPoolExecutor.CallerRunsPolicy {
        return object : ThreadPoolExecutor.CallerRunsPolicy() {
            override fun rejectedExecution(r: Runnable, executor: ThreadPoolExecutor) {
                CoreLogger.w(TAG, "rejected on $slot, running on caller thread; pool=$executor")
                super.rejectedExecution(r, executor)
            }
        }
    }
}
