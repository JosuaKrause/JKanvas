package jkanvas.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;

/**
 * An action that defines the behavior of an animated object when an animation
 * has finished.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public abstract class AnimationAction {

  /**
   * A list of actions held on a thread local storage. The actions are separated
   * by {@link AnimationToken animation tokens}.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  private static final class ThreadLocalList {

    /** The token map. */
    private final Map<AnimationToken, List<AnimationAction>> tokenMap;
    /** The current active list. */
    private List<AnimationAction> cur;

    /** Creates a thread local list. */
    public ThreadLocalList() {
      tokenMap = new WeakHashMap<>();
    }

    /**
     * Setter.
     * 
     * @param token Sets the current token and the current active list.
     */
    public void setToken(final AnimationToken token) {
      while((cur = tokenMap.get(token)) == null) {
        final List<AnimationAction> list = new ArrayList<>();
        token.register(list);
        tokenMap.put(token, list);
      }
    }

    /**
     * Enqueues an action to the current active list. The method assumes that
     * prior a call to {@link #setToken(AnimationToken)} has been made.
     * 
     * @param action The action to enqueue.
     */
    public void enqueue(final AnimationAction action) {
      cur.add(action);
    }

  } // ThreadLocalAction

  /** The thread local storage. */
  private static final ThreadLocal<ThreadLocalList> THREAD_LOCAL = new ThreadLocal<ThreadLocalList>() {

    @Override
    protected ThreadLocalList initialValue() {
      return new ThreadLocalList();
    }

  };

  /**
   * Setter.
   * 
   * @param token Sets the current active token for this thread.
   */
  public static final void setToken(final AnimationToken token) {
    Objects.requireNonNull(token);
    final ThreadLocalList list = THREAD_LOCAL.get();
    list.setToken(token);
  }

  /**
   * Enqueues an action in the current active thread local action list.
   * 
   * @param action The animation action.
   */
  public static final void enqueue(final AnimationAction action) {
    Objects.requireNonNull(action);
    final ThreadLocalList list = THREAD_LOCAL.get();
    list.enqueue(action);
  }

  /**
   * Is called when the animation with which it was registered terminates. This
   * may be when the animation terminates successfully or when the animation
   * gets overwritten. This method should not called directly unless from within
   * another {@link AnimationAction}.
   */
  public abstract void animationFinished();

}
