package jkanvas.util;

import java.io.PrintStream;
import java.util.Objects;

/**
 * Performs benchmarks for given algorithms.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class Benchmark {

  /** The benchmarks. */
  private final BenchmarkExecutor[] executors;

  /** The configuration title. */
  private final String title = "configuration";

  /** The mean title. */
  private final String meanTitle = "mean";

  /** The standard deviation title. */
  private final String stddevTitle = "stddev";

  /** The description column width. */
  private final int descriptionLength;

  /** The mean column width. */
  private final int meanLength;

  /** The standard deviation column width. */
  private final int stddevLength;

  /**
   * An algorithm to be bench-marked. An implementation of this interface must
   * be state-less in a way that the {@link #execute()} method may be called
   * multiple times and always computing everything again. The configuration may
   * not be altered after handing a {@link BenchmarkExecutor} into a
   * {@link Benchmark}.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  public static interface BenchmarkExecutor {

    /**
     * Getter.
     * 
     * @return The description of how this executor is configured. For same
     *         executors this string should have a fixed width. For the same
     *         configuration this method should always return the same
     *         {@link String}.
     */
    String getConfigurationString();

    /**
     * Executes the algorithm. This method will be called multiple times and
     * must always perform the same computation.
     */
    void execute();

  } // BenchmarkExecutor

  /**
   * Creates a benchmark for the given algorithms.
   * 
   * @param executors The algorithms.
   */
  public Benchmark(final BenchmarkExecutor[] executors) {
    this.executors = Objects.requireNonNull(executors);
    int length = title.length();
    for(final BenchmarkExecutor e : executors) {
      length = Math.max(e.getConfigurationString().length(), length);
    }
    descriptionLength = length;
    meanLength = Math.max(meanTitle.length(), 15);
    stddevLength = Math.max(stddevTitle.length(), 19);
  }

  /**
   * Fills the string builder with the given character.
   * 
   * @param builder The string builder.
   * @param c The character.
   * @param count The number of characters to fill.
   */
  private static void fill(final StringBuilder builder, final char c, final int count) {
    builder.ensureCapacity(builder.length() + count);
    for(int i = 0; i < count; ++i) {
      builder.append(c);
    }
  }

  /**
   * The headline.
   * 
   * @param builder The string builder.
   */
  private void getHeadLine(final StringBuilder builder) {
    builder.append("| ");
    builder.append(title);
    fill(builder, ' ', descriptionLength - title.length());
    builder.append(" | ");
    fill(builder, ' ', meanLength - meanTitle.length());
    builder.append(meanTitle);
    builder.append(" | ");
    fill(builder, ' ', stddevLength - stddevTitle.length());
    builder.append(stddevTitle);
    builder.append(" |\n");
  }

  /**
   * The delimiter.
   * 
   * @param builder The string builder.
   */
  private void getDelimiter(final StringBuilder builder) {
    builder.append("+-");
    fill(builder, '-', descriptionLength);
    builder.append("-+-");
    fill(builder, '-', meanLength);
    builder.append("-+-");
    fill(builder, '-', stddevLength);
    builder.append("-+\n");
  }

  /**
   * Gets the line for the current benchmark.
   * 
   * @param builder The string builder.
   * @param e The executor.
   * @param time The mean time in nanoseconds.
   * @param stddev The standard deviation in nanoseconds.
   */
  private void getLineFor(final StringBuilder builder, final BenchmarkExecutor e,
      final double time, final double stddev) {
    builder.append("| ");
    final String conf = e.getConfigurationString();
    builder.append(conf);
    fill(builder, ' ', descriptionLength - conf.length());
    builder.append(String.format(" | %12.6f ms | +/- %12.6f ms |", time * 1e-6,
        stddev * 1e-6));
    builder.append('\n');
  }

  /** The number of executions in the warm-up phase. */
  private int warmUp = 20;

  /** The number of executions in the benchmark phase. */
  private int benchmark = 50;

  /**
   * Performs the benchmark for a given algorithm.
   * 
   * @param builder The string builder.
   * @param progress The progress is print here.
   * @param e The algorithm.
   */
  private void doBenchmark(final StringBuilder builder,
      final PrintStream progress, final BenchmarkExecutor e) {
    progress.println("Warm-Up:");
    int lastP = 0;
    for(int i = 0; i < warmUp; ++i) {
      final int p = i * 4 / warmUp;
      if(lastP < p) {
        progress.println(((double) i) / warmUp * 100.0 + " %");
      }
      lastP = p;
      e.execute();
    }
    progress.println("100.0 %");
    double sum = 0;
    final double[] times = new double[benchmark];
    final Stopwatch timer = new Stopwatch();
    lastP = 0;
    progress.println("Benchmark:");
    for(int i = 0; i < benchmark; ++i) {
      final int p = i * 4 / warmUp;
      if(lastP < p) {
        progress.println(((double) i) / benchmark * 100.0 + " %");
      }
      lastP = p;
      timer.start();
      e.execute();
      final double curTime = timer.currentNano();
      times[i] = curTime;
      sum += curTime;
    }
    progress.println("100.0 %");
    final double mean = sum / benchmark;
    double stddev = 0;
    for(final double t : times) {
      final double c = t - mean;
      stddev += c * c;
    }
    getLineFor(builder, e, mean, Math.sqrt(stddev / (benchmark - 1)));
  }

  /**
   * Performs the benchmark.
   * 
   * @param out A nicely formatted statistic is print here.
   * @param progress The progress is print here.
   */
  public void getResults(final PrintStream out, final PrintStream progress) {
    final StringBuilder builder = new StringBuilder();
    getDelimiter(builder);
    getHeadLine(builder);
    getDelimiter(builder);
    int i = 0;
    for(final BenchmarkExecutor e : executors) {
      progress.println("Benchmark #" + i + ":");
      doBenchmark(builder, progress, e);
      progress.println();
      ++i;
    }
    getDelimiter(builder);
    out.print(builder.toString());
  }

  /**
   * Setter.
   * 
   * @param warmUp The number of executions in the warm-up phase.
   */
  public void setWarmUp(final int warmUp) {
    this.warmUp = warmUp;
  }

  /**
   * Getter.
   * 
   * @return The number of executions in the warm-up phase.
   */
  public int getWarmUp() {
    return warmUp;
  }

  /**
   * Setter.
   * 
   * @param benchmark The number of executions in the benchmark phase.
   */
  public void setBenchmark(final int benchmark) {
    this.benchmark = benchmark;
  }

  /**
   * Getter.
   * 
   * @return The number of executions in the benchmark phase.
   */
  public int getBenchmark() {
    return benchmark;
  }

}
