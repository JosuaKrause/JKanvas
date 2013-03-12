package jkanvas.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
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

  /** The animation lock. */
  private final Object lock;

  /**
   * Creates an animation list.
   * 
   * @param lock The animation lock.
   */
  public AnimationList(final Object lock) {
    this.lock = lock;
  }

  /** All registered animated objects. */
  private final List<Animated> animated = new ArrayList<>();

  /** A quick check to see whether an object is in the list. */
  private final Set<Animated> quickCheck = Collections.newSetFromMap(new IdentityHashMap<Animated, Boolean>());

  /**
   * Adds an animatable object.
   * 
   * @param animate The object.
   */
  public void addAnimated(final Animated animate) {
    synchronized(lock) {
      if(quickCheck.contains(animate)) throw new IllegalArgumentException(
          "animated object already added: " + animate);
      quickCheck.add(animate);
      animated.add(animate);
    }
  }

  /**
   * Removes an animatable object.
   * 
   * @param animate The object.
   */
  public void removeAnimated(final Animated animate) {
    synchronized(lock) {
      if(quickCheck.remove(animate)) {
        animated.remove(animate);
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
    synchronized(lock) {
      has = quickCheck.contains(animate);
    }
    return has;
  }

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

  }

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
    // animation lock is already acquired
    final boolean needsRedraw;
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
    return needsRedraw;
  }

}
