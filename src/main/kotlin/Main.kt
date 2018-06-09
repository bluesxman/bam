import Tracker.Companion.now
import com.github.kittinunf.fuel.Fuel
import kotlinx.coroutines.experimental.Deferred
import kotlinx.coroutines.experimental.async
import kotlinx.coroutines.experimental.runBlocking

fun main(args : Array<String>) {
    val stats = runBlocking {
        requestConcurrent(
            url = "http://d2i1u3uf4t4nuo.cloudfront.net/index.html",
            concurrency = 100,
            iterations = 1000
        ).await()
    }
    d(stats.toString())
}

fun requestConcurrent(url: String, concurrency: Int, iterations: Int) = async {
    val jobs = mutableListOf<Deferred<Stats>>()
    val start = now()
    val overall = Tracker()

    for (i in 1..concurrency) {
        d("Concurrent=$i")
        jobs.add(requestSequentially(url, iterations, i))
    }

    jobs.forEach {
        overall.accumulate(it.await())
    }

    d("c=$concurrency i=$iterations")
    d("Time to complete = ${now() - start}")

    overall.stats()
}

fun requestSequentially(url: String, iterations: Int, id: Int): Deferred<Stats> = async {
    val tracker = Tracker()

    repeat(iterations, { i ->
        tracker.start()
        d("request id=$id iteration=$i")
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