package jkanvas.util;

import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;

import jkanvas.util.Benchmark.BenchmarkExecutor;

/**
 * Benchmarks the coroutine performance.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class CoRoutineBenchmark {

  /**
   * A coroutine benchmark task.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  private static final class CoRoutineExecutor implements BenchmarkExecutor {

    /** The number of items. */
    protected final int numberOfItems;
    /** The random number generator. */
    protected final Random rnd = new Random();

    /**
     * Creates a coroutine executor.
     * 
     * @param numberOfItems The number of items to iterate.
     */
    public CoRoutineExecutor(final int numberOfItems) {
      this.numberOfItems = numberOfItems;
    }

    @Override
    public String getConfigurationString() {
      return "thread[count: " + numberOfItems + "]";
    }

    @Override
    public void execute() {
      final AtomicInteger sum = new AtomicInteger();
      final CoRoutine<Integer> routine = new CoRoutine<Integer>() {

        @Override
        protected void compute() {
          int s = 0;
          for(int i = 0; i < numberOfItems; ++i) {
            final int cur = rnd.nextInt();
            yield(cur);
            s += cur;
          }
          sum.set(s);
          yield(0);
        }

      };
      int ownSum = 0;
      while(routine.hasNext()) {
        ownSum += routine.next();
      }
      if(ownSum != sum.get()) throw new IllegalStateException(
          "sums must be equal: " + ownSum + " != " + sum);
    }

  } // CoRoutineExecutor

  /**
   * Performs the benchmark.
   * 
   * @param args No arguments.
   */
  public static void main(final String[] args) {
    final CoRoutineExecutor[] benchmarks = {
        new CoRoutineExecutor(0), // #1
        new CoRoutineExecutor(1), // #2
        new CoRoutineExecutor(100), // #3
        new CoRoutineExecutor(2000), // #4
        new CoRoutineExecutor(10000), // #5
        new CoRoutineExecutor(1000000), // #6
    };
    final Benchmark benchmark = new Benchmark(benchmarks);
    benchmark.getResults(System.out, System.err);
  }

  /**
   * <pre>
   * Benchmark on Mac OS X 10.9
   * 2.3 GHz Intel Core i7
   * 8GB Ram 1600 MHz DDR3
   * no VM arguments
   * ==================================================================
   * coroutine with thread and linked blocking queue
   * ==================================================================
   * +------------------------+-----------------+---------------------+
   * | configuration          |            mean |              stddev |
   * +------------------------+-----------------+---------------------+
   * | thread[count: 0]       |     0.111140 ms | +/-     0.023123 ms |
   * | thread[count: 1]       |     0.111560 ms | +/-     0.014795 ms |
   * | thread[count: 100]     |     0.637260 ms | +/-     0.144068 ms |
   * | thread[count: 2000]    |     1.520080 ms | +/-     0.354738 ms |
   * | thread[count: 10000]   |     6.049480 ms | +/-     1.572290 ms |
   * | thread[count: 1000000] |   755.040160 ms | +/-    61.527500 ms |
   * +------------------------+-----------------+---------------------+
   * ==================================================================
   * coroutine with thread and array blocking queue
   * (Note that, although the times are very similar, the array
   * blocking queue always has a fixed amount of space requirement)
   * ==================================================================
   * | thread[count: 0]       |     0.109800 ms | +/-     0.017158 ms |
   * | thread[count: 1]       |     0.106480 ms | +/-     0.009314 ms |
   * | thread[count: 100]     |     0.600560 ms | +/-     0.170556 ms |
   * | thread[count: 2000]    |     1.373600 ms | +/-     0.480028 ms |
   * | thread[count: 10000]   |     7.259980 ms | +/-     1.352968 ms |
   * | thread[count: 1000000] |   860.554820 ms | +/-    73.994132 ms |
   * +------------------------+-----------------+---------------------+
   * </pre>
   */

}
