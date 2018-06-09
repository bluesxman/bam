import java.lang.Long.max
import java.lang.Long.min

class Tracker {
    private var count = 0
    private var failCount = 0
    private var totalTime = 0L
    private var minTime = Long.MAX_VALUE
    private var maxTime = -1L
    private var startTime = 0L

    fun start() {
        count++
        startTime = now()
    }

    fun stop() {
        val delta = now() - startTime
        totalTime += delta
        minTime = min(minTime, delta)
        maxTime = max(maxTime, delta)
    }

    fun fail() {
        failCount++
    }

    fun stats() = Stats(
        total = totalTime,
        count = count,
        avg = totalTime / count,
        min = minTime,
        max = maxTime,
        fails = failCount
    )

    fun accumulate(stats: Stats) {
        count += stats.count
        failCount += stats.fails
        totalTime += stats.total
        minTime = min(minTime, stats.min)
        maxTime = max(maxTime, stats.max)
    }

    companion object {
        fun now() = System.currentTimeMillis()
    }
}

data class Stats(
    val count: Int,
    val total: Long,
    val avg: Long,
    val min: Long,
    val max: Long,
    val fails: Int
)