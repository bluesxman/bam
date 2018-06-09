import Tracker.Companion.now
import com.github.kittinunf.fuel.Fuel
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking

const val BAM_ENDPOINT_ENV="BAM_ENDPOINT"

fun main(args : Array<String>) {
    val endpoint = getEndpoint() ?: return

    val stats = runBlocking {
        requestConcurrent(
            url = endpoint,
            concurrency = 10,
            iterations = 1000
        ).await()
    }

    d(stats.toString())
}

fun requestConcurrent(url: String, concurrency: Int, iterations: Int) = async {
    val futureStats = mutableListOf<Deferred<Stats>>()
    val overall = Tracker()
    val start = now()

    for (i in 1..concurrency) {
        d("Concurrent=$i")
        futureStats.add(measureRequestSequence(url, iterations, i))
    }

    futureStats.forEach {
        overall.accumulate(it.await())
    }

    d("c=$concurrency i=$iterations")
    d("Time to complete = ${now() - start}")

    overall.stats()
}

fun measureRequestSequence(url: String, iterations: Int, id: Int): Deferred<Stats> = async {
    val tracker = Tracker()

    repeat(iterations, { i ->
        d("request id=$id iteration=$i")
        tracker.start()
        try {
            request(url)
        } catch (e: Exception) {
            tracker.fail()
            d("fail id=$id iteration=$i errorType=${e.javaClass.name} error=${e.message}")
        }
        tracker.stop()
    })

    tracker.stats()
}

suspend fun request(url: String) = Fuel.get(url).awaitResponse()

fun d(msg: String) = System.out.println(msg)

fun getEndpoint(): String? = System.getenv(BAM_ENDPOINT_ENV) ?: run {
    d("Environment variable $BAM_ENDPOINT_ENV not set")
    null
}