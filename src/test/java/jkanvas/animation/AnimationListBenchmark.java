package jkanvas.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import jkanvas.util.Benchmark;
import jkanvas.util.Benchmark.BenchmarkExecutor;
import jkanvas.util.Interpolator;

/**
 * Benchmarks the animation list performance.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class AnimationListBenchmark {

  /**
   * An action for the benchmark.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  private static final class BenchmarkAction extends AnimationAction {

    /** The hardness of the action. */
    private final int hardness;
    /** The sum. */
    public long sum;

    /**
     * Creates an action for the given hardness.
     * 
     * @param hardness The hardness.
     */
    public BenchmarkAction(final int hardness) {
      this.hardness = hardness;
    }

    /**
     * Performs a summing or subtracting operation.
     * 
     * @return The sum.
     */
    private long sum() {
      long sum = 0;
      for(int i = 0; i < hardness; ++i) {
        sum += i;
      }
      return sum;
    }

    @Override
    public void animationFinished() {
      sum = sum();
    }

  } // BenchmarkAction

  /**
   * An animation list benchmark task.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  private static final class AnimationListExecutor implements BenchmarkExecutor {

    /** The number of animated objects. */
    private final int numTasks;
    /** The duration of the animation of the objects. */
    private final long taskDuration;
    /** The chance whether an action is performed on finishing. */
    private final double actionChance;
    /** The number of scheduled actions. */
    private final int numSchedules;
    /** The point when the scheduled actions will be performed. */
    private final long actionDuration;
    /** The hardness of an action in terms of numbers to sum up. */
    private final int actionHardness;

    /**
     * Creates an benchmark task.
     * 
     * @param numTasks The number of animated objects.
     * @param taskDuration The duration of the animation of the objects.
     * @param actionChance The chance to perform an action after the animation.
     * @param numSchedules The number of scheduled actions.
     * @param actionDuration The time when the actions will be executed.
     * @param actionHardness The hardness of an action in terms of numbers to
     *          sum up.
     */
    public AnimationListExecutor(final int numTasks, final long taskDuration,
        final double actionChance, final int numSchedules,
        final long actionDuration, final int actionHardness) {
      this.numTasks = numTasks;
      this.taskDuration = taskDuration;
      this.actionChance = actionChance;
      this.numSchedules = numSchedules;
      this.actionDuration = actionDuration;
      this.actionHardness = actionHardness;
      animated = createArray(numTasks);
      ini();
    }

    /**
     * Getter.
     * 
     * @param length The length of the array.
     * @return Creates an array of the given length.
     */
    @SuppressWarnings("unchecked")
    private static GenericAnimated<Double>[] createArray(final int length) {
      return new GenericAnimated[length];
    }

    @Override
    public String getConfigurationString() {
      return "tasks[count: " + numTasks + " duration: " + taskDuration
          + "] action[count: " + numSchedules + " duration: " + actionDuration
          + " chance: " + actionChance + " hardness: " + actionHardness + " ]";
    }

    /** The random number generator. */
    private final Random rnd = new Random();

    /**
     * Getter.
     * 
     * @return Creates an action.
     */
    private BenchmarkAction createAction() {
      return new BenchmarkAction(actionHardness);
    }

    /** The animation list. */
    private final AnimationList list = new AnimationList();
    /** The array to store the actions. */
    private final GenericAnimated<Double>[] animated;

    /** Initializes the benchmark once. */
    private void ini() {
      for(int i = 0; i < animated.length; ++i) {
        // TODO #43 -- Java 8 simplification
        final GenericAnimated<Double> a = new GenericAnimated<Double>(0.0) {

          @Override
          protected Double interpolate(final Double from, final Double to, final double t) {
            return from * (1 - t) + to * t;
          }

        };
        list.addAnimated(a);
        animated[i] = a;
      }
    }

    @Override
    public void execute() {
      final List<BenchmarkAction> actions = new ArrayList<>();
      final AnimationTiming timing = new AnimationTiming(
          Interpolator.SMOOTH, taskDuration);
      for(int i = 0; i < animated.length; ++i) {
        final GenericAnimated<Double> a = animated[i];
        final BenchmarkAction action;
        if(rnd.nextDouble() < actionChance) {
          action = createAction();
          actions.add(action);
        } else {
          action = null;
        }
        a.set(0.0);
        a.startAnimationTo(1.0, timing, action);
      }
      final AnimationTiming actionTiming = new AnimationTiming(
          Interpolator.SMOOTH, actionDuration);
      for(int i = 0; i < numSchedules; ++i) {
        final BenchmarkAction action = createAction();
        list.scheduleAction(action, actionTiming);
        actions.add(action);
      }
      final long iterations = Math.max(taskDuration, actionDuration) + 10L;
      for(long i = 0; i < iterations; ++i) {
        list.doAnimate(i);
      }
      final long sum = (actionHardness - 1L) * (actionHardness) / 2L;
      for(final BenchmarkAction action : actions) {
        if(action.sum != sum) throw new IllegalStateException("missed action: " + action);
      }
    }

  } // AnimationListExecutor

  /**
   * Performs the benchmark.
   * 
   * @param args No arguments.
   */
  public static void main(final String[] args) {
    final AnimationListExecutor[] benchmarks = {
        new AnimationListExecutor(10, 1000, .5, 0, 500, 1000000), // #0
        new AnimationListExecutor(10, 1000, 0, 10, 500, 1000), // #1
        new AnimationListExecutor(10, 1000, 1, 10, 500, 1000), // #1
        new AnimationListExecutor(1000, 1000, .5, 100, 1000, 1000), // #2
        new AnimationListExecutor(10000, 1000, .5, 100, 1000, 1000), // #3
    };
    final Benchmark benchmark = new Benchmark(benchmarks);
    benchmark.getResults(System.out, System.err);
  }

  /**
   * <pre>
   * Benchmark on Mac OS X 10.8.3
   * 2.66 GHz Intel Core i7
   * 8GB Ram 1067 MHz DDR3
   * no VM arguments
   * =============================
   * parallel execution
   * =============================
   * +--------------------------------------------------------------------------------------------------+-----------------+---------------------+
   * | configuration                                                                                    |            mean |              stddev |
   * +--------------------------------------------------------------------------------------------------+-----------------+---------------------+
   * | tasks[count: 10 duration: 1000] action[count: 0 duration: 500 chance: 0.5 hardness: 1000000 ]    |     3,379400 ms | +/-     0,756371 ms |
   * | tasks[count: 10 duration: 1000] action[count: 10 duration: 500 chance: 0.0 hardness: 1000 ]      |     1,301360 ms | +/-     0,957903 ms |
   * | tasks[count: 10 duration: 1000] action[count: 10 duration: 500 chance: 1.0 hardness: 1000 ]      |     1,145800 ms | +/-     0,552058 ms |
   * | tasks[count: 1000 duration: 1000] action[count: 100 duration: 1000 chance: 0.5 hardness: 1000 ]  |    68,929720 ms | +/-     6,339674 ms |
   * | tasks[count: 10000 duration: 1000] action[count: 100 duration: 1000 chance: 0.5 hardness: 1000 ] |   471,164540 ms | +/-    33,193038 ms |
   * +--------------------------------------------------------------------------------------------------+-----------------+---------------------+
   * =============================
   * sequential execution
   * =============================
   * +--------------------------------------------------------------------------------------------------+-----------------+---------------------+
   * | configuration                                                                                    |            mean |              stddev |
   * +--------------------------------------------------------------------------------------------------+-----------------+---------------------+
   * | tasks[count: 10 duration: 1000] action[count: 0 duration: 500 chance: 0.5 hardness: 1000000 ]    |     3,517360 ms | +/-     1,184715 ms |
   * | tasks[count: 10 duration: 1000] action[count: 10 duration: 500 chance: 0.0 hardness: 1000 ]      |     1,217100 ms | +/-     0,732385 ms |
   * | tasks[count: 10 duration: 1000] action[count: 10 duration: 500 chance: 1.0 hardness: 1000 ]      |     1,118680 ms | +/-     0,509277 ms |
   * | tasks[count: 1000 duration: 1000] action[count: 100 duration: 1000 chance: 0.5 hardness: 1000 ]  |    69,031580 ms | +/-     5,682776 ms |
   * | tasks[count: 10000 duration: 1000] action[count: 100 duration: 1000 chance: 0.5 hardness: 1000 ] |   679,798680 ms | +/-    15,851845 ms |
   * +--------------------------------------------------------------------------------------------------+-----------------+---------------------+
   * </pre>
   */

}
