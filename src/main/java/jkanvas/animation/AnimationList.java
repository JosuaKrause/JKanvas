package jkanvas.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveAction;

/**
 * An animation list holds all animated objects. Animated objects by themselves
 * must not hold other animated objects. A flat view is required to perform
 * suitable optimizations.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public final class AnimationList {

  /** All registered animated objects. */
  private final List<Animated> animated = new ArrayList<>();

  /**
   * A quick check to see whether an object is in the list. This set is also
   * used as monitor for all list changing operations and setting
   * {@link #inAnimation}.
   */
  private final Set<Animated> quickCheck = Collections.newSetFromMap(new IdentityHashMap<Animated, Boolean>());

  /**
   * The list that is filled when {@link #inAnimation} is <code>true</code>
   * instead of {@link #animated}. The elements are added when
   * {@link #inAnimation} is set to <code>false</code>.
   */
  private final List<Animated> toBeAdded = new ArrayList<>();

  /**
   * The list that schedules removal when {@link #inAnimation} is
   * <code>true</code> and {@link #animated} cannot be changed. The elements are
   * removed when {@link #inAnimation} is set to <code>false</code>.
   */
  private final List<Animated> toBeRemoved = new ArrayList<>();

  /**
   * When set to <code>true</code> the list {@link #animated} is guaranteed not
   * to change.
   */
  private volatile boolean inAnimation;

  /**
   * Creates an animation list.
   */
  public AnimationList() {
    inAnimation = false;
  }

  // ### managing animatable objects ###

  /**
   * Adds an animatable object.
   * 
   * @param animate The object.
   */
  public void addAnimated(final Animated animate) {
    synchronized(quickCheck) {
      if(quickCheck.contains(animate)) throw new IllegalArgumentException(
          "animated object already added: " + animate);
      quickCheck.add(animate);
      if(inAnimation) {
        toBeAdded.add(animate);
      } else {
        animated.add(animate);
      }
    }
  }

  /**
   * Removes an animatable object.
   * 
   * @param animate The object.
   */
  public void removeAnimated(final Animated animate) {
    synchronized(quickCheck) {
      if(quickCheck.remove(animate)) {
        if(inAnimation) {
          if(!toBeAdded.contains(animate)) {
            // object must be in animated
            toBeRemoved.add(animate);
          } else {
            toBeAdded.remove(animate);
          }
        } else {
          animated.remove(animate);
        }
      }
    }
  }

  /**
   * Getter.
   * 
   * @param animate The animated object.
   * @return Whether the animated object is contained in the list.
   */
  public boolean hasAnimated(final Animated animate) {
    final boolean has;
    synchronized(quickCheck) {
      has = quickCheck.contains(animate);
    }
    return has;
  }

  /**
   * Starts the animation phase. This method needs a corresponding
   * {@link #endAnimating()} call which must be in a finally block.
   */
  private void startAnimating() {
    synchronized(quickCheck) {
      inAnimation = true;
    }
  }

  /** Ends the animation phase and alters {@link #animated}. */
  private void endAnimating() {
    synchronized(quickCheck) {
      inAnimation = false;
      // remove first because objects could be re-added
      animated.removeAll(toBeRemoved);
      toBeRemoved.clear();
      animated.addAll(toBeAdded);
      toBeAdded.clear();
    }
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
  private final class Worker extends RecursiveAction {

    /** Whether at least one animated object has been changed. */
    private boolean changed;

    /** The start position of this worker. */
    private final int start;

    /** The exclusive end position of this worker. */
    private final int end;

    /** The depth of the worker. If 0 the worker does the computation. */
    private final int depth;

    /**
     * Creates a worker to animate objects.
     * 
     * @param depth The depth of this worker. If it reaches 0 the worker
     *          actually computes the result.
     * @param start The start position.
     * @param end The exclusive end position.
     */
    public Worker(final int depth, final int start, final int end) {
      this.depth = depth;
      this.start = start;
      this.end = end;
    }

    @Override
    protected void compute() {
      if(depth <= 0) {
        changed = AnimationList.this.compute(start, end);
        return;
      }
      final int mid = (start + end) >>> 1;
      final Worker left = new Worker(depth - 1, start, mid);
      final Worker right = new Worker(depth - 1, mid, end);
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
   * @param from The start index inclusive.
   * @param to The end index exclusive.
   * @return Whether a redraw is needed.
   */
  boolean compute(final int from, final int to) {
    int pos = from;
    boolean hasChanged = false;
    while(pos < to) {
      hasChanged |= animated.get(pos).animate(currentTime);
      ++pos;
    }
    return hasChanged;
  }

  /** The current time. */
  long currentTime;

  /**
   * Computes one step for all animated.
   * 
   * @param currentTime The current time in milliseconds.
   * @return Whether a redraw is needed.
   */
  boolean doAnimate(final long currentTime) {
    final boolean needsRedraw;
    try {
      startAnimating();
      this.currentTime = currentTime;
      final int size = animated.size();
      final int depth = suggestDepth(size);
      if(depth <= 0) {
        needsRedraw = compute(0, size);
      } else {
        // a worker has a higher idle time (ie when no animation is happening)
        // but when we animate it is faster than the sequential version
        final Worker task = new Worker(depth, 0, size);
        pool.invoke(task);
        needsRedraw = task.hasChanged();
      }
      processActions(currentTime);
    } finally {
      endAnimating();
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
