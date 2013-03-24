package jkanvas.animation;

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
    private AnimationAction createAction() {
      final int hardness = actionHardness;
      return new AnimationAction() {

        @Override
        public void animationFinished() {
          @SuppressWarnings("unused")
          int sum = 0;
          for(int i = 0; i < hardness; ++i) {
            sum += i;
          }
        }

      };
    }

    /** The animation list. */
    private final AnimationList list = new AnimationList();
    /** The array to store the actions. */
    private final GenericAnimated<Double>[] animated;

    /** Initializes the benchmark once. */
    private void ini() {
      for(int i = 0; i < animated.length; ++i) {
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
      final AnimationTiming timing = new AnimationTiming(
          Interpolator.SMOOTH, taskDuration);
      for(int i = 0; i < animated.length; ++i) {
        final GenericAnimated<Double> a = animated[i];
        final AnimationAction action;
        if(rnd.nextDouble() < actionChance) {
          action = createAction();
        } else {
          action = null;
        }
        a.set(0.0);
        a.startAnimationTo(1.0, timing, action);
      }
      final AnimationTiming actionTiming = new AnimationTiming(
          Interpolator.SMOOTH, actionDuration);
      for(int i = 0; i < numSchedules; ++i) {
        list.scheduleAction(createAction(), actionTiming);
      }
      final long iterations = Math.max(taskDuration, actionDuration) + 10L;
      for(long i = 0; i < iterations; ++i) {
        list.doAnimate(iterations);
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
        new AnimationListExecutor(10, 1000, 0, 0, 500, 1000), // #0
        new AnimationListExecutor(10, 1000, .3, 10, 500, 1000), // #1
        new AnimationListExecutor(10, 1000, .6, 10, 500, 1000), // #2
        new AnimationListExecutor(10, 1000, 1, 10, 500, 1000), // #3
        new AnimationListExecutor(1000, 1000, 0, 100, 1000, 1000), // #4
        new AnimationListExecutor(1000, 1000, .3, 100, 1000, 1000), // #5
        new AnimationListExecutor(1000, 1000, .6, 100, 1000, 1000), // #6
        new AnimationListExecutor(1000, 1000, 1, 100, 1000, 1000), // #7
        new AnimationListExecutor(10000, 1000, .5, 100, 1000, 1000), // #8
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
   * | tasks[count: 10 duration: 1000] action[count: 0 duration: 500 chance: 0.0 hardness: 1000 ]       |    20,833380 ms | +/-     1,718439 ms |
   * | tasks[count: 10 duration: 1000] action[count: 10 duration: 500 chance: 0.3 hardness: 1000 ]      |    48,320260 ms | +/-     6,349045 ms |
   * | tasks[count: 10 duration: 1000] action[count: 10 duration: 500 chance: 0.6 hardness: 1000 ]      |    46,250000 ms | +/-     5,146774 ms |
   * | tasks[count: 10 duration: 1000] action[count: 10 duration: 500 chance: 1.0 hardness: 1000 ]      |    46,375980 ms | +/-     5,094906 ms |
   * | tasks[count: 1000 duration: 1000] action[count: 100 duration: 1000 chance: 0.0 hardness: 1000 ]  |   212,188160 ms | +/-    44,612220 ms |
   * | tasks[count: 1000 duration: 1000] action[count: 100 duration: 1000 chance: 0.3 hardness: 1000 ]  |   213,122620 ms | +/-    45,326996 ms |
   * | tasks[count: 1000 duration: 1000] action[count: 100 duration: 1000 chance: 0.6 hardness: 1000 ]  |   210,592520 ms | +/-    44,002505 ms |
   * | tasks[count: 1000 duration: 1000] action[count: 100 duration: 1000 chance: 1.0 hardness: 1000 ]  |   213,236920 ms | +/-    45,218314 ms |
   * | tasks[count: 10000 duration: 1000] action[count: 100 duration: 1000 chance: 0.5 hardness: 1000 ] |   573,501020 ms | +/-    69,913724 ms |
   * +--------------------------------------------------------------------------------------------------+-----------------+---------------------+
   * =============================
   * sequential execution
   * =============================
   * +--------------------------------------------------------------------------------------------------+-----------------+---------------------+
   * | configuration                                                                                    |            mean |              stddev |
   * +--------------------------------------------------------------------------------------------------+-----------------+---------------------+
   * | tasks[count: 10 duration: 1000] action[count: 0 duration: 500 chance: 0.0 hardness: 1000 ]       |     1,092320 ms | +/-     0,866097 ms |
   * | tasks[count: 10 duration: 1000] action[count: 10 duration: 500 chance: 0.3 hardness: 1000 ]      |    14,775540 ms | +/-     4,986127 ms |
   * | tasks[count: 10 duration: 1000] action[count: 10 duration: 500 chance: 0.6 hardness: 1000 ]      |    14,914860 ms | +/-     4,931201 ms |
   * | tasks[count: 10 duration: 1000] action[count: 10 duration: 500 chance: 1.0 hardness: 1000 ]      |    14,261280 ms | +/-     5,894706 ms |
   * | tasks[count: 1000 duration: 1000] action[count: 100 duration: 1000 chance: 0.0 hardness: 1000 ]  |   171,166760 ms | +/-    38,442687 ms |
   * | tasks[count: 1000 duration: 1000] action[count: 100 duration: 1000 chance: 0.3 hardness: 1000 ]  |   173,057680 ms | +/-    38,635925 ms |
   * | tasks[count: 1000 duration: 1000] action[count: 100 duration: 1000 chance: 0.6 hardness: 1000 ]  |   173,705880 ms | +/-    38,453111 ms |
   * | tasks[count: 1000 duration: 1000] action[count: 100 duration: 1000 chance: 1.0 hardness: 1000 ]  |   172,804840 ms | +/-    38,841471 ms |
   * | tasks[count: 10000 duration: 1000] action[count: 100 duration: 1000 chance: 0.5 hardness: 1000 ] |   770,142080 ms | +/-    74,611816 ms |
   * +--------------------------------------------------------------------------------------------------+-----------------+---------------------+
   * </pre>
   */

}
