package utils

object BenchmarkUtils {
  /**
   * Runs a block of code, measures its execution time, and prints the result.
   *
   * @param description A description of the code block being benchmarked.
   * @param block The code block to execute.
   * @tparam R The return type of the code block.
   * @return The result of the code block.
   */
  def time[R](description: String)(block: => R): R = {
    val t0 = System.nanoTime()
    val result = block    // call-by-name
    val t1 = System.nanoTime()
    val elapsedMs = (t1 - t0) / 1000000.0
    println(s"⏱️ [Benchmark] $description: $elapsedMs ms")
    result
  }
}
