package app

import akka.actor.{Actor, ActorLogging, Props}

class ConcurrentStringSearchRunner(times: Int) extends Actor with ActorLogging {

  val buffer = scala.collection.mutable.Buffer.empty[Long]
  var fails = 0
  var nanoStart: Long = 0

  override def receive: Receive = idle

  def idle: Receive = {
    case msg: Run =>
      import msg._

      nanoStart = System.nanoTime() // READY STEADY

      context.become(searching(input.size, msg))
      input foreach {
        chunk =>
          context.actorOf(Props[StringSearchActor]) ! Search(sub, chunk)
      }
  }

  def searching(n: Int, msg: Any): Receive = {
    case result: Boolean => result match {
      case true =>
        reset(found = true, msg)
      case false =>
        fails += 1
        if (fails == n) reset(found = false, msg)
    }
  }

  def reset(found: Boolean, msg: Any): Unit = {
    val delta = System.nanoTime() - nanoStart
    context.children.foreach(child => context.stop(child))
    context.become(idle)
    fails = 0
    buffer += delta

    if (buffer.size == times) {
      val avgDeltaNs = buffer.sum / buffer.size
      log.info("" + avgDeltaNs)
      context.system.terminate()
    } else {
      self ! msg // restart
    }
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