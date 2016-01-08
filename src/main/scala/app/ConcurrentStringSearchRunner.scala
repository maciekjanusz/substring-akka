package app

import akka.actor.{Actor, ActorLogging, Props}

case class Run(sub: String, input: Seq[String])

case class Search(sub: String, input: String)

class StringSearchActor extends Actor with ActorLogging {

  override def receive: Receive = {
    case Search(sub, input) =>
      // run the contains method, stop self and return result to parent
      val result = input contains sub
      context.stop(self)
      sender ! result
  }
}

class ConcurrentStringSearchRunner extends Actor with ActorLogging {

  var fails = 0

  override def receive: Receive = idle

  def idle: Receive = {
    case msg: Run =>
      import msg._
      // create processing actor for each chunk and await results
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
    // cleanup and terminate
    context.children.foreach(child => context.stop(child))
    context.become(idle)
    context.system.terminate()
  }
}