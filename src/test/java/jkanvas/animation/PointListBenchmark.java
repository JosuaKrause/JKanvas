package jkanvas.animation;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Random;

import jkanvas.util.Benchmark;
import jkanvas.util.Benchmark.BenchmarkExecutor;

/**
 * Benchmarks the point list performance.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class PointListBenchmark {

  /**
   * An point list benchmark task.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  private static final class PointListExecutor implements BenchmarkExecutor {

    /** Whether to use a point list or an array. */
    private final boolean usePointList;
    /** The point list. */
    private final PointList list;
    /** The array */
    private final Ellipse2D[] array;
    /** The number of items. */
    private final int numberOfItems;
    /**
     * The element that is expected to hit or <code>-1</code> if no element will
     * hit.
     */
    private final int expected;
    /** The number of gaps. */
    private final int gaps;
    /** The random number generator. */
    private final Random rnd = new Random();

    /**
     * Creates a benchmark task.
     * 
     * @param numberOfItems The number of items.
     * @param numberOfGaps The number of gaps.
     * @param expectedElement The expected element.
     * @param usePointList Whether to use the point list.
     */
    public PointListExecutor(final int numberOfItems, final int numberOfGaps,
        final int expectedElement, final boolean usePointList) {
      this.numberOfItems = numberOfItems;
      gaps = numberOfGaps;
      expected = expectedElement;
      this.usePointList = usePointList;
      if(usePointList) {
        array = null;
        list = new PointList(numberOfItems, null, null);
        // elements will not contain the point (2.0, 2.0)
        for(int i = 0; i < numberOfItems; ++i) {
          final double x = rnd.nextDouble();
          final double y = rnd.nextDouble();
          final double s = rnd.nextDouble();
          list.addPoint(x, y, s);
        }
        for(int i = 0; i < gaps; ++i) {
          final int index = rnd.nextInt(numberOfItems);
          if(index == expected) {
            continue;
          }
          list.removeIndex(index);
        }
        if(expected >= 0) {
          list.setPoint(expected, 2.0, 2.0, 1.0);
        }
      } else {
        list = null;
        array = new Ellipse2D[numberOfItems];
        // elements will not contain the point (2.0, 2.0)
        for(int i = 0; i < numberOfItems; ++i) {
          final double x = rnd.nextDouble();
          final double y = rnd.nextDouble();
          final double s = rnd.nextDouble();
          array[i] = new Ellipse2D.Double(x - s, y - s, s * 2, s * 2);
        }
        for(int i = 0; i < gaps; ++i) {
          final int index = rnd.nextInt(numberOfItems);
          if(index == expected) {
            continue;
          }
          array[index] = null;
        }
        if(expected >= 0) {
          array[expected].setFrame(1.0, 1.0, 2.0, 2.0);
        }
      }
    }

    @Override
    public String getConfigurationString() {
      return (usePointList ? "plist" : "array") + "[count: " + numberOfItems
          + " gaps: " + gaps + " res: " + expected + "]";
    }

    @Override
    public void execute() {
      final Point2D point = new Point2D.Double(2.0, 2.0);
      final int index;
      if(usePointList) {
        index = list.hit(point);
      } else {
        int res = -1;
        for(int i = 0; i < array.length; ++i) {
          if(array[i] == null) {
            continue;
          }
          if(array[i].contains(point)) {
            res = i;
            break;
          }
        }
        index = res;
      }
      if(expected < 0 && index >= 0) throw new IllegalStateException(
          "point must not be contained");
      else if(index != expected) throw new IllegalStateException(
          "element " + expected + " must be found got " + index);
    }

  } // PointListExecutor

  /**
   * Performs the benchmark.
   * 
   * @param args No arguments.
   */
  public static void main(final String[] args) {
    final boolean usePointList = true;
    final PointListExecutor[] benchmarks = {
        new PointListExecutor(10000, 0, -1, usePointList), // #1
        new PointListExecutor(10000, 100, -1, usePointList), // #2
        new PointListExecutor(10000, 0, 6000, usePointList), // #3
        new PointListExecutor(10000, 100, 6000, usePointList), // #4
        new PointListExecutor(10000, 3000, 6000, usePointList), // #5
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
   * =============================
   * no point list
   * =============================
   * +------------------------------------------+-----------------+---------------------+
   * | configuration                            |            mean |              stddev |
   * +------------------------------------------+-----------------+---------------------+
   * | array[count: 10000 gaps: 0 res: -1]      |     0.085560 ms | +/-     0.003737 ms |
   * | array[count: 10000 gaps: 100 res: -1]    |     0.103480 ms | +/-     0.018481 ms |
   * | array[count: 10000 gaps: 0 res: 6000]    |     0.052140 ms | +/-     0.004136 ms |
   * | array[count: 10000 gaps: 100 res: 6000]  |     0.051600 ms | +/-     0.004271 ms |
   * | array[count: 10000 gaps: 3000 res: 6000] |     0.039540 ms | +/-     0.002541 ms |
   * +------------------------------------------+-----------------+---------------------+
   * =============================
   * parallel arrays
   * =============================
   * +------------------------------------------+-----------------+---------------------+
   * | configuration                            |            mean |              stddev |
   * +------------------------------------------+-----------------+---------------------+
   * | plist[count: 10000 gaps: 0 res: -1]      |     0.118960 ms | +/-     0.012871 ms |
   * | plist[count: 10000 gaps: 100 res: -1]    |     0.138340 ms | +/-     0.004939 ms |
   * | plist[count: 10000 gaps: 0 res: 6000]    |     0.070580 ms | +/-     0.008389 ms |
   * | plist[count: 10000 gaps: 100 res: 6000]  |     0.075320 ms | +/-     0.004479 ms |
   * | plist[count: 10000 gaps: 3000 res: 6000] |     0.065760 ms | +/-     0.001697 ms |
   * +------------------------------------------+-----------------+---------------------+
   * </pre>
   */

}
