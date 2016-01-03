package app

import java.util.concurrent.TimeUnit


class BlockingStringSearchRunner {

  def search(sub: String, input: String): Unit = {
    val nanoStart = System.nanoTime()

    val found = input.contains(sub)

    val delta = System.nanoTime() - nanoStart
    val deltaMillis = TimeUnit.NANOSECONDS.toMillis(delta)

    println("Finished in " + deltaMillis + " ms (" + delta + "ns)")
    if (found) {
      println("Found!")
    } else {
      println("Not found!")
    }
  }
}