package app

import java.io.File

import akka.actor.{ActorSystem, Props}
import org.apache.commons.io.FileUtils

object SubstringApp {
  val nChunks = 4
  val textfile = FileUtils.readFileToString(new File("400m.txt"))
  val sub = "V3zbkLszG2DFIR0U5IyE"
  val repeats = 10000
  val min = 10000
  val max = 1000000
  val step = min

  def main(args: Array[String]) {
    runAkka(max)
    //    runBlocking()
  }

  def runAkka(len: Int): Unit = {
    val input = textfile.substring(0, len)
    val chunkSize = input.length / nChunks
    val chunkExtra = sub.length - 1
    val chunks = for (i <- 0 until nChunks)
      yield {
        val subStart = i * chunkSize
        val end = subStart + chunkSize + chunkExtra
        val subEnd =
          if (i == nChunks - 1 || end > input.length) input.length
          else end
        input.substring(subStart, subEnd)
      }

    val actorSystem = ActorSystem()
    val runner = actorSystem.actorOf(Props(classOf[ConcurrentStringSearchRunner], repeats))
    actorSystem.registerOnTermination {
      if (len > min) {
        runAkka(len - step)
      }
    }
    print(input.length + " ")
    runner ! Run(sub, chunks)
  }

  def runBlocking() {
    val range = min to max by step
    range foreach { len =>
      val input = textfile.substring(0, len)
      new BlockingStringSearchRunner(repeats).search(sub, input)
    }
  }
}
