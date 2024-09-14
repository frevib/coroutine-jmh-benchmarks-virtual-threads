package com.eventloopsoftware

import io.ktor.server.netty.*
import io.vertx.core.Vertx
import io.vertx.kotlin.coroutines.coAwait
import io.vertx.kotlin.coroutines.dispatcher
import kotlinx.coroutines.*
import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import org.openjdk.jmh.profile.JavaFlightRecorderProfiler
import org.openjdk.jmh.runner.Runner
import org.openjdk.jmh.runner.options.Options
import org.openjdk.jmh.runner.options.OptionsBuilder
import java.lang.Thread.sleep
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit
import kotlin.concurrent.thread
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.random.Random

// run main function to use options like addProfiler(...)
fun main() {
    val options: Options = OptionsBuilder()
        .include(DispatchersContextSwitchBenchmark::class.java.simpleName)
        .addProfiler(JavaFlightRecorderProfiler::class.java)
        .build()
    Runner(options).run()
}

@Warmup(iterations = 5, time = 1, timeUnit = TimeUnit.SECONDS)
@Measurement(iterations = 5, time = 2, timeUnit = TimeUnit.SECONDS)
@Fork(value = 1)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
//@Threads(1)
@State(Scope.Thread)
open class DispatchersContextSwitchBenchmark {

    private val CORES_COUNT = Runtime.getRuntime().availableProcessors()

//    @Param("10000", "50000", "100000")
    private var nCoroutines: Int = 1000000
    private val delayTimeMs = 1L
    private val nRepeatDelay = 10

    @Param("100", "1000")
    private var timesRandInt: Int = 1000

    private val fjp = ForkJoinPool.commonPool().asCoroutineDispatcher()
    private val ftp = Executors.newFixedThreadPool(CORES_COUNT - 1).asCoroutineDispatcher()
    private val vt = Executors.newVirtualThreadPerTaskExecutor().asCoroutineDispatcher()

//    private val vertx = Vertx.vertx()
//    private val vertxDispatcher = vertx.dispatcher()
    private val nettyEventLoopGroup = EventLoopGroupProxy.create(CORES_COUNT - 1).asCoroutineDispatcher()


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

//    @Benchmark
//    fun coroutinesIoDispatcher(bh: Blackhole) = runBenchmark(Dispatchers.IO, bh)

    @Benchmark
    fun coroutinesDefaultDispatcher(bh: Blackhole) = runBenchmark(Dispatchers.Default, bh)

    @Benchmark
    fun coroutinesUnconfinedDispatcher(bh: Blackhole) = runBenchmark(Dispatchers.Unconfined, bh)

//    @Benchmark
//    fun coroutinesFjpDispatcher(bh: Blackhole) = runBenchmark(fjp)
//
//    @Benchmark
//    fun coroutinesFtpDispatcher(bh: Blackhole) = runBenchmark(ftp)

    @Benchmark
    fun coroutinesVtDispatcher(bh: Blackhole) = runBenchmark(vt, bh)

    @Benchmark
    fun coroutinesBlockingDispatcher(bh: Blackhole) = runBenchmark(EmptyCoroutineContext, bh)

//    @Benchmark
//    fun coroutinesVertxDispatcher(bh: Blackhole) = runBenchmark(Vertx.vertx().dispatcher(), bh)

    @Benchmark
    fun coroutinesEventloopGroupDispatcher(bh: Blackhole) = runBenchmark(nettyEventLoopGroup, bh)

//    @Benchmark
//    fun threads(bh: Blackhole) {
//        val threads: List<Thread> = List(nCoroutines) {
//            thread(start = true) {
//                repeat(nRepeatDelay) {
//                    sleep(delayTimeMs)
//                    val compute: List<Int> = (0..1000).map {
//                        it + Random.nextInt()
//                    }
//                    bh.consume(compute)
//                }
//            }
//        }
//        threads.forEach { it.join() }
//    }

//    private fun runBenchmark(dispatcher: CoroutineContext, bh: Blackhole) = runBlocking {
//        repeat(nCoroutines) {
//            launch(dispatcher) {
//                repeat(nRepeatDelay) {
//                    delay(delayTimeMs)
//                    val compute: List<Int> = (0..1000).map {
//                        it + Random.nextInt()
//                    }
//                    bh.consume(compute)
//                }
//            }
//        }
//    }

    private fun runBenchmark(dispatcher: CoroutineContext, bh: Blackhole)  = runBlocking {
        repeat(nCoroutines) {
            launch(dispatcher) {
                repeat(nRepeatDelay) {
                    delay(delayTimeMs)
                }
            }
        }
    }
}

