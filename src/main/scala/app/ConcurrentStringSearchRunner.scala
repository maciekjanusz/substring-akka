package app

import java.util.concurrent.TimeUnit

import akka.actor.{Actor, ActorLogging, Props}

class ConcurrentStringSearchRunner extends Actor with ActorLogging {

  var fails = 0
  var nanoStart: Long = 0

  override def receive: Receive = idle

  def idle: Receive = {
    case Run(sub, chunks) =>
      context.become(searching(chunks.size))
      nanoStart = System.nanoTime()
      chunks foreach {
        chunk =>
          context.actorOf(Props[StringSearchActor]) ! Search(sub, chunk)
      }
  }

  def searching(n: Int): Receive = {
    case result: Boolean => result match {
      case true =>
        stop(found = true)
      case false =>
        fails += 1
        if (fails == n) stop(found = false)
    }
  }

  def stop(found: Boolean): Unit = {
    val delta = System.nanoTime() - nanoStart
    val deltaMillis = TimeUnit.NANOSECONDS.toMillis(delta)

    log.info("Finished (" + found + ") in " + deltaMillis + " ms (" + delta + "ns)")
    context.children.foreach(context.stop)
    fails = 0
    context.become(idle)
  }
}

case class Run(sub: String, input: Seq[String])

case class Search(sub: String, input: String)

case class Result(contains: Boolean)

class StringSearchActor extends Actor with ActorLogging {

  override def receive: Receive = {
    case Search(sub, input) =>
      val result = input contains sub
      sender ! result
      context.stop(self)
  }
}