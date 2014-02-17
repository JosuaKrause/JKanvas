package jkanvas.util;

/**
 * A simple class to measure the computation time of tasks.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class Stopwatch {

  /** The nano time of the last call to start. */
  private long lastStart;

  /** Creates a new timer and starts it. */
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
    return formatNano(currentNano());
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

  /**
   * Prints the given message and the current duration of the task to the given
   * output stream. The timer is reset afterwards.
   * 
   * @param msg The message.
   * @param out The output stream.
   */
  public void status(final String msg, final java.io.PrintStream out) {
    out.println(msg + reset());
  }

  /**
   * Formats the given time in nano seconds.
   * 
   * @param nano The time in nano seconds.
   * @return The formatted time (in milliseconds).
   */
  public static final String formatNano(final long nano) {
    final double duration = nano / 1000.0 / 1000.0;
    return String.format("%.6f ms", duration);
  }

}
