package app

import akka.actor.{Actor, ActorLogging, Props}

class ConcurrentStringSearchRunner extends Actor with ActorLogging {

  var fails = 0

  override def receive: Receive = idle

  def idle: Receive = {
    case msg: Run =>
      import msg._

      context.become(searching(input.size))
      input foreach {
        chunk =>
          context.actorOf(Props[StringSearchActor]) ! Search(sub, chunk)
      }
  }

  def searching(n: Int): Receive = {
    case result: Boolean => result match {
      case true =>
        reset(found = true)
      case false =>
        fails += 1
        if (fails == n) reset(found = false)
    }
  }

  def reset(found: Boolean): Unit = {
    log.info("Result: " + found)
    context.children.foreach(child => context.stop(child))
    context.become(idle)
    context.system.terminate()
  }
}

case class Run(sub: String, input: Seq[String])

case class Search(sub: String, input: String)

class StringSearchActor extends Actor with ActorLogging {

  override def receive: Receive = {
    case Search(sub, input) =>
      val result = input contains sub
      context.stop(self)
      sender ! result
  }
}