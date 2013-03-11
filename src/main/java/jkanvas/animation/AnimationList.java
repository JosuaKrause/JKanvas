package jkanvas.animation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;

/**
 * An animation list holds all animated objects. Animated objects by themselves
 * must not hold other animated objects. A flat view is required to perform
 * suitable optimizations.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class AnimationList {

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

  /**
   * Computes one step for all animated.
   * 
   * @param currentTime The current time in milliseconds.
   * @return Whether a redraw is needed.
   */
  boolean doAnimate(final long currentTime) {
    // TODO use a ForkJoinPool
    boolean needsRedraw = false;
    // animation lock is already acquired
    for(final Animated a : animated) {
      needsRedraw = a.animate(currentTime) || needsRedraw;
    }
    return needsRedraw;
  }

}
