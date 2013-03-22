package jkanvas.animation;

import java.util.Objects;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

import jkanvas.util.SnapshotList;
import jkanvas.util.SnapshotList.Snapshot;

/**
 * An animation list holds all animated objects. Animated objects by themselves
 * must not hold other animated objects. A flat view is required to perform
 * suitable optimizations.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public final class AnimationList {

  /** The list containing the animatable objects. */
  private final SnapshotList<Animated> animated;

  /** Creates an animation list. */
  public AnimationList() {
    animated = new SnapshotList<>();
  }

  // ### managing animatable objects ###

  /**
   * Adds an animatable object.
   * 
   * @param animate The object.
   */
  public void addAnimated(final Animated animate) {
    animated.add(animate);
  }

  // ### performing the animation ###

  /** The internal fork join pool. */
  private final ForkJoinPool pool = new ForkJoinPool();

  /**
   * Suggests the deep of a parallel computation tree. This method is copied
   * from <code>java.concurrent.ForkJoinUtils</code> which does not exist in
   * Java 7.
   * 
   * @param size The size of the array to split.
   * @return The depth of the parallel computation tree.
   */
  private int suggestDepth(final long size) {
    long s = size;
    final long leafSize = 1 + ((s + 7) >>> 3) / pool.getParallelism();
    int d = 0;
    while(s > leafSize) {
      s /= 2;
      ++d;
    }
    return d;
  }

  /**
   * A worker animating objects when under a certain threshold or splitting the
   * task further.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  private static final class Worker extends RecursiveAction {

    /** The current snapshot of animated objects. */
    private final Snapshot<Animated> list;

    /** The current time in milliseconds. */
    private final long currentTime;

    /** The start position of this worker. */
    private final int start;

    /** The exclusive end position of this worker. */
    private final int end;

    /** The depth of the worker. If 0 the worker does the computation. */
    private final int depth;

    /** Whether at least one animated object has been changed. */
    private boolean changed;

    /**
     * Creates a worker to animate objects.
     * 
     * @param list The current snapshot of animated objects.
     * @param currentTime The current time in milliseconds.
     * @param depth The depth of this worker. If it reaches 0 the worker
     *          actually computes the result.
     * @param start The start position.
     * @param end The exclusive end position.
     */
    public Worker(final Snapshot<Animated> list, final long currentTime,
        final int depth, final int start, final int end) {
      this.list = list;
      this.currentTime = currentTime;
      this.depth = depth;
      this.start = start;
      this.end = end;
    }

    @Override
    protected void compute() {
      if(depth <= 0) {
        changed = AnimationList.compute(list, start, end, currentTime);
        return;
      }
      final int mid = (start + end) >>> 1;
      final Worker left = new Worker(list, currentTime, depth - 1, start, mid);
      final Worker right = new Worker(list, currentTime, depth - 1, mid, end);
      right.fork();
      left.compute();
      right.join();
      changed = left.changed || right.changed;
    }

    /**
     * Getter.
     * 
     * @return Whether the computation has changed any animated object.
     */
    public boolean hasChanged() {
      return changed;
    }

  } // Worker

  /**
   * Animates a range of animated objects.
   * 
   * @param s The current list snapshot.
   * @param from The start index inclusive.
   * @param to The end index exclusive.
   * @param currentTime The current time in milliseconds.
   * @return Whether a redraw is needed.
   */
  static boolean compute(final Snapshot<Animated> s,
      final int from, final int to, final long currentTime) {
    int pos = from;
    boolean hasChanged = false;
    while(pos < to) {
      final Animated e = s.get(pos);
      if(e != null) {
        hasChanged |= e.animate(currentTime);
      }
      ++pos;
    }
    return hasChanged;
  }

  /**
   * Computes one step for all animated.
   * 
   * @param currentTime The current time in milliseconds.
   * @return Whether a redraw is needed.
   */
  boolean doAnimate(final long currentTime) {
    final boolean needsRedraw;
    try (Snapshot<Animated> s = animated.getSnapshot()) {
      final int size = s.size();
      final int depth = suggestDepth(size);
      if(depth <= 0) {
        needsRedraw = compute(s, 0, size, currentTime);
      } else {
        // a worker has a higher idle time (ie when no animation is happening)
        // but when we animate it is faster than the sequential version
        final Worker task = new Worker(s, currentTime, depth, 0, size);
        pool.invoke(task);
        needsRedraw = task.hasChanged();
      }
      processActions(currentTime);
    }
    return needsRedraw;
  }

  // ### managing delayed actions ###

  /**
   * A timed action that is executed when its due.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  private static final class TimedAction {

    /** The action to execute. */
    private final AnimationAction action;
    /** Whether the timing is relative. */
    private boolean relative;
    /** The relative or absolute due time. */
    private long due;

    /**
     * Creates a timed action.
     * 
     * @param action The action. Must not be <code>null</code>.
     * @param wait The time to wait in milliseconds.
     */
    public TimedAction(final AnimationAction action, final long wait) {
      this.action = Objects.requireNonNull(action);
      relative = true;
      due = wait > 0 ? wait : 0;
    }

    /**
     * Converts a relative action in to an absolute action. If the action is due
     * it gets executed and needs not be to rescheduled any more.
     * 
     * @param currentTime The current time.
     * @return Whether to reschedule.
     */
    public boolean execute(final long currentTime) {
      if(relative) {
        due += currentTime;
        relative = false;
      }
      if(currentTime < due) return true;
      action.animationFinished();
      return false;
    }

  } // TimedAction

  /** The queue containing all actions. */
  private final Queue<TimedAction> actionQueue = new ConcurrentLinkedQueue<TimedAction>();

  /**
   * Schedules the given action to be executed after the specified time in
   * milliseconds.
   * 
   * @param action The action to be executed. May be <code>null</code> when no
   *          action needs to be executed.
   * @param timing The timing to infer the duration.
   */
  public void scheduleAction(final AnimationAction action, final AnimationTiming timing) {
    scheduleAction(action, timing.duration);
  }

  /**
   * Schedules the given action to be executed after the specified time in
   * milliseconds.
   * 
   * @param action The action to be executed. May be <code>null</code> when no
   *          action needs to be executed.
   * @param wait The time to wait in milliseconds.
   */
  public void scheduleAction(final AnimationAction action, final long wait) {
    if(action == null) return;
    actionQueue.offer(new TimedAction(action, wait));
  }

  /**
   * Processes the actions.
   * 
   * @param currentTime The current time.
   */
  private void processActions(final long currentTime) {
    // we work on the same queue for relative and absolute actions
    // therefore we have to go through the whole list each time
    // this is not that expensive because there are usually only few action at a
    // time
    TimedAction first = null;
    TimedAction cur;
    for(;;) {
      cur = actionQueue.poll();
      if(cur == null) return;
      if(cur == first) {
        actionQueue.offer(cur);
        return;
      }
      final boolean reschedule = cur.execute(currentTime);
      if(reschedule) {
        actionQueue.offer(cur);
        if(first == null) {
          first = cur;
        }
      }
    }
  }

}
