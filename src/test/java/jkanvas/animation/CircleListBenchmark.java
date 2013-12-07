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
public class CircleListBenchmark {

  /**
   * An point list benchmark task.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  private static final class PointListExecutor implements BenchmarkExecutor {

    /** Whether to use a point list or an array. */
    private final boolean usePointList;
    /** The point list. */
    private final CircleList list;
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
        list = new CircleList(numberOfItems, null, null);
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
          // this one should not be picked
          if(expected > 0) {
            array[expected - 1].setFrame(1.0, 1.0, 2.0, 2.0);
          }
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
        // emulate correct hit order
        for(int i = array.length - 1; i >= 0; --i) {
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
        new PointListExecutor(1000000, 0, -1, usePointList), // #1
        new PointListExecutor(1000000, 10000, -1, usePointList), // #2
        new PointListExecutor(1000000, 0, 200000, usePointList), // #3
        new PointListExecutor(1000000, 10000, 200000, usePointList), // #4
        new PointListExecutor(1000000, 300000, 200000, usePointList), // #5
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
   * +------------------------------------------------+-----------------+---------------------+
   * | configuration                                  |            mean |              stddev |
   * +------------------------------------------------+-----------------+---------------------+
   * | array[count: 1000000 gaps: 0 res: -1]          |     8.794160 ms | +/-     0.237009 ms |
   * | array[count: 1000000 gaps: 10000 res: -1]      |     8.676320 ms | +/-     0.129667 ms |
   * | array[count: 1000000 gaps: 0 res: 600000]      |     5.314820 ms | +/-     0.126771 ms |
   * | array[count: 1000000 gaps: 10000 res: 600000]  |     5.187620 ms | +/-     0.142014 ms |
   * | array[count: 1000000 gaps: 300000 res: 600000] |     3.966420 ms | +/-     0.406973 ms |
   * +------------------------------------------------+-----------------+---------------------+
   * =============================
   * parallel arrays
   * =============================
   * +------------------------------------------------+-----------------+---------------------+
   * | configuration                                  |            mean |              stddev |
   * +------------------------------------------------+-----------------+---------------------+
   * | plist[count: 1000000 gaps: 0 res: -1]          |     8.560460 ms | +/-     0.212078 ms |
   * | plist[count: 1000000 gaps: 10000 res: -1]      |     8.580680 ms | +/-     0.143697 ms |
   * | plist[count: 1000000 gaps: 0 res: 600000]      |     5.137220 ms | +/-     0.122974 ms |
   * | plist[count: 1000000 gaps: 10000 res: 600000]  |     5.150300 ms | +/-     0.113594 ms |
   * | plist[count: 1000000 gaps: 300000 res: 600000] |     5.097120 ms | +/-     0.227816 ms |
   * +------------------------------------------------+-----------------+---------------------+
   * =============================
   * single array
   * =============================
   * +------------------------------------------------+-----------------+---------------------+
   * | configuration                                  |            mean |              stddev |
   * +------------------------------------------------+-----------------+---------------------+
   * | plist[count: 1000000 gaps: 0 res: -1]          |     8.659640 ms | +/-     0.213654 ms |
   * | plist[count: 1000000 gaps: 10000 res: -1]      |     8.640600 ms | +/-     0.216042 ms |
   * | plist[count: 1000000 gaps: 0 res: 600000]      |     5.161840 ms | +/-     0.120085 ms |
   * | plist[count: 1000000 gaps: 10000 res: 600000]  |     5.169940 ms | +/-     0.120099 ms |
   * | plist[count: 1000000 gaps: 300000 res: 600000] |     4.807160 ms | +/-     0.138717 ms |
   * +------------------------------------------------+-----------------+---------------------+
   * </pre>
   */

}
