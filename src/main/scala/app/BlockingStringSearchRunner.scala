package app

class BlockingStringSearchRunner(times: Long) {

  val buffer = scala.collection.mutable.Buffer.empty[Long]

  def search(sub: String, input: String): Unit = {
    0L until times foreach { _ =>
      val nanoStart = System.nanoTime()
      val found = input contains sub
      val delta = System.nanoTime() - nanoStart
      buffer += delta
    }

    val avgDelta = buffer.sum / buffer.size
    println("" + input.length + " " + avgDelta)
  }
}