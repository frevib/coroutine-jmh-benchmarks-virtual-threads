package com.eventloopsoftware

import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*
import java.lang.Thread.sleep
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Thread)
open class DispatchersContextSwitchBenchmark {

    private val CORES_COUNT = Runtime.getRuntime().availableProcessors()
    private val nCoroutines = 10000
    private val delayTimeMs = 10L
    private val nRepeatDelay = 10

    private val fjp = ForkJoinPool.commonPool().asCoroutineDispatcher()
    private val ftp = Executors.newFixedThreadPool(CORES_COUNT - 1).asCoroutineDispatcher()
    private val vtp = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()

    @TearDown
    fun teardown() {
        ftp.close()
        (ftp.executor as ExecutorService).awaitTermination(1, TimeUnit.SECONDS)

        println("Spec:")
        println("Core count : ${CORES_COUNT}")
        println("Number of coroutines/threads: ${nCoroutines}")
        println("Delay time ms: ${delayTimeMs}")
        println("Repeat delay number: ${nRepeatDelay}")
    }

    @Benchmark
    fun coroutinesIoDispatcher() = runBenchmark(Dispatchers.IO)

    @Benchmark
    fun coroutinesDefaultDispatcher() = runBenchmark(Dispatchers.Default)

    @Benchmark
    fun coroutinesUnconfinedDispatcher() = runBenchmark(Dispatchers.Unconfined)

    @Benchmark
    fun coroutinesFjpDispatcher() = runBenchmark(fjp)

    @Benchmark
    fun coroutinesFtpDispatcher() = runBenchmark(ftp)

    @Benchmark
    fun coroutinesVtDispatcher() = runBenchmark(vtp)

    @Benchmark
    fun coroutinesBlockingDispatcher() = runBenchmark(EmptyCoroutineContext)

    @Benchmark
    fun threads() {
        val threads: List<Thread> = List(nCoroutines) {
            thread(start = true) {
                repeat(nRepeatDelay) {
                    sleep(delayTimeMs)
                }
            }
        }
        threads.forEach { it.join() }
    }

    private fun runBenchmark(dispatcher: CoroutineContext) = runBlocking {
        repeat(nCoroutines) {
            launch(dispatcher) {
                repeat(nRepeatDelay) {
                    delay(delayTimeMs)
                }
            }
        }
    }
}

