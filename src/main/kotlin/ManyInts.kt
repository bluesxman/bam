import kotlinx.coroutines.experimental.delay
import kotlinx.coroutines.experimental.launch
import kotlinx.coroutines.experimental.runBlocking

fun main(args: Array<String>) = runBlocking {
    println("Starting to create coroutines")
    val jobs = List(100_000) { index ->
        launch {
            delay(5000L)
            println(index)
        }
    }
    println("All coroutines created")
    jobs.forEach { it.join() } // wait for all jobs to complete
    println("All coroutines finished")
}