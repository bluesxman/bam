import com.github.kittinunf.fuel.Fuel
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking

fun main(args : Array<String>) {
    val stats = runBlocking {
        requestConcurrent(
            url = "http://d2i1u3uf4t4nuo.cloudfront.net/index.html",
            concurrency = 10,
            iterations = 1000).await()
    }
    d(stats.toString())
}

fun requestConcurrent(url: String, concurrency: Int, iterations: Int) = async {
    val jobs = mutableListOf<Deferred<Stats>>()
    val start = now()
    var totalAvg = 0L
    var overallMax = -1L
    var overallMin = Long.MAX_VALUE
    var overallFails = 0

    for (i in 1..concurrency) {
//        d("Concurrent=$i")
        jobs.add(requestAll(url, iterations, i))
    }

    jobs.forEach {
        with(it.await()) {
            totalAvg += avg
            overallFails += fails
            if (min < overallMin) {
                overallMin = min
            }
            if (max > overallMax) {
                overallMax = max
            }
        }
    }

    d("c=$concurrency i=$iterations")
    d("total time = ${now() - start}")

    Stats(
        avg = totalAvg / concurrency,
        min = overallMin,
        max = overallMax,
        fails = overallFails)
}

fun requestAll(url: String, iterations: Int, id: Int): Deferred<Stats> = async {
    var total = 0L
    var max = -1L
    var min = Long.MAX_VALUE
    var start: Long
    var time: Long
    var fails = 0

    repeat(iterations, { i ->
        start = now()
//        d("request id=$id iteration=$i")
        try {
            request(url)
        } catch (e: Exception) {
            fails++
            d("fail id=$id iteration=$i errorType=${e.javaClass.name} error=${e.message}")
        }
        time = now() - start
        total += time
        if (time < min) {
            min = time
        }
        if (time > max) {
            max = time
        }
    })

    Stats(
        avg = total / iterations,
        min = min,
        max = max,
        fails = fails
    )
}

// val (request, response, result) = request(url)
suspend fun request(url: String) = Fuel.get(url).awaitResponse()

fun d(msg: String) = System.out.println(msg)

fun now() = System.currentTimeMillis()

data class Stats(
    val avg: Long,
    val min: Long,
    val max: Long,
    val fails: Int
)