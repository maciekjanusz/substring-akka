package app

import java.io.File

import akka.actor.{ActorSystem, Props}
import org.apache.commons.io.FileUtils

object SubstringApp {

  def main(args: Array[String]): Unit = {
    if (args.length < 2) {
      println("required arguments: <substring> <filepath>")
      return
    }

    val mode = args(0)
    val sub = args(1)
    val filepath = args(2)
    val nChunks = args(3).toInt
    val file = new File(filepath)
    val input = FileUtils.readFileToString(file)
    val actorSystem = ActorSystem()

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

    println("" + input.length + " characters")

    if (mode.equals("a")) {
      val runner = actorSystem.actorOf(Props(classOf[ConcurrentStringSearchRunner]), name = "runner")
      runner ! Run(sub, chunks)
    } else {
      new BlockingStringSearchRunner().search(sub, input)
    }
  }
}
