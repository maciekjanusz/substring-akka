package app

import java.io.File

import akka.actor.{ActorSystem, Props}
import org.apache.commons.io.FileUtils

object SubstringApp {

  def main(args: Array[String]): Unit = {
    val sub = "V3zbkLszG2DFIR0U5IyE"
    val nChunks = 4
    val input = FileUtils.readFileToString(new File("400m.txt"))
    val chunkSize = input.length / nChunks
    val chunkExtra = sub.length - 1

    val chunks = for (i <- 0 until nChunks) yield {
      val subStart = i * chunkSize
      val end = subStart + chunkSize + chunkExtra
      val subEnd =
        if (i == nChunks - 1 || end > input.length) input.length
        else end
      input.substring(subStart, subEnd)
    }

    new BlockingStringSearchRunner().search(sub, input)

    val actorSystem = ActorSystem()
    val runner = actorSystem.actorOf(Props(classOf[ConcurrentStringSearchRunner]))
    runner ! Run(sub, chunks)
  }
}
