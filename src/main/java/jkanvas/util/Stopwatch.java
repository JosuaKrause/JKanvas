package jkanvas.util;

/**
 * A simple class to measure the computation time of tasks.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class Stopwatch {

  /** The nano time of the last call to start. */
  private long lastStart;

  /** Creates a new timer. */
  public Stopwatch() {
    start();
  }

  /** Resets the timer. */
  public void start() {
    lastStart = System.nanoTime();
  }

  /**
   * Returns the current duration of the task without resetting.
   * 
   * @return The duration in nano seconds.
   */
  public long currentNano() {
    return System.nanoTime() - lastStart;
  }

  /**
   * Returns the current duration of the task without resetting.
   * 
   * @return The duration as human readable string.
   */
  public String current() {
    final double duration = currentNano() / 1000.0 / 1000.0;
    return String.format("%.6f ms", duration);
  }

  /**
   * Returns the current duration of the task and resets the timer.
   * 
   * @return The duration as human readable string.
   */
  public String reset() {
    final String res = current();
    start();
    return res;
  }

}
