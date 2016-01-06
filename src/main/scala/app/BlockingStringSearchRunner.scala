package app

class BlockingStringSearchRunner {

  def search(sub: String, input: String): Unit = {
    val found = input contains sub
    println("Result: " + found)
  }
}