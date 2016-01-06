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

  //  def main(args: Array[String]): Unit = {
  //    val mode = args(0)
  //    val sub = args(1)
  //    val filepath = args(2)
  //    val nChunks = args(3).toInt
  //    val repeats = args(4).toInt
  //    val n = args(5).toInt
  //    val file = new File(filepath)
  //    val input = FileUtils.readFileToString(file).substring(0, n)
  //    val chunkSize = input.length / nChunks
  //    val chunkExtra = sub.length - 1
  //
  //    val chunks = for (i <- 0 until nChunks)
  //      yield {
  //        val subStart = i * chunkSize
  //        val end = subStart + chunkSize + chunkExtra
  //        val subEnd =
  //          if (i == nChunks - 1 || end > input.length) input.length
  //          else end
  //        input.substring(subStart, subEnd)
  //      }
  //
  //    println("N = " + (input.length/1000000) + "mln, M = " + sub.length + ", repeats = " + repeats)
  //
  //    if (mode.equals("a")) {
  //      val actorSystem = ActorSystem()
  //      val runner = actorSystem.actorOf(Props(classOf[ConcurrentStringSearchRunner], repeats))
  //      runner ! Run(sub, chunks)
  //    } else {
  //      new BlockingStringSearchRunner(repeats).search(sub, input)
  //    }
  //  }
}
