package jkanvas.animation;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

import jkanvas.Canvas;
import jkanvas.util.Stopwatch;

/**
 * An animation barrier coordinates drawing and animating.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class AnimationBarrier {

  // drawing: (ALLOW_ALL | ALLOW_DRAW) -> IN_DRAWING -> ALLOW_ALL
  // animation: ALLOW_ALL -> IN_ANIMATION -> (ALLOW_DRAW | ALLOW_ALL)

  // bits of states should not overlap since states can be combined with or

  /** Signals an invalid state. */
  static final int INVALID = -1;

  /** Signals an state that will always be accepted. */
  static final int DONT_CARE = 0b0000;

  /** Signals to allow animation and drawing. */
  static final int ALLOW_ALL = 0b0001;

  /** Signals to allow only drawing. */
  static final int ALLOW_DRAW = 0b0010;

  /** Signals that an animation currently is in progress. */
  static final int IN_ANIMATION = 0b0100;

  /** Signals that an drawing operation currently is in progress. */
  static final int IN_DRAWING = 0b1000;

  /** The object to wait on. */
  private final Object barrier;

  /**
   * The owning canvas. This field is only used to ensure validity and is
   * cleared afterwards.
   */
  private Canvas canvas;

  /** The current state. */
  private volatile int state;

  /**
   * Creates an animation barrier for the given canvas. This method should only
   * called from within the given canvas.
   * 
   * @param canvas The canvas.
   */
  public AnimationBarrier(final Canvas canvas) {
    this.canvas = Objects.requireNonNull(canvas);
    state = ALLOW_ALL;
    barrier = new Object();
  }

  /**
   * Ensures the validity of the barrier. This method may only be called once.
   * 
   * @param canvas The owning canvas.
   */
  public void ensureValidity(final Canvas canvas) {
    if(this.canvas == null) throw new IllegalStateException(
        "barrier may only be installed once");
    if(this.canvas != canvas) throw new IllegalArgumentException(
        "invalid canvas: " + canvas);
    this.canvas = null;
  }

  /**
   * Setter.
   * 
   * @param newState The new state.
   * @param expected The expected state. If it is {@link #DONT_CARE} the old
   *          state is always accepted.
   */
  protected void setState(final int newState, final int expected) {
    if(newState == INVALID) throw new IllegalArgumentException("invalid not allowed");
    synchronized(barrier) {
      final int s = state;
      assert s != INVALID;
      if(expected != DONT_CARE && (s & expected) != s) throw new IllegalStateException(
          "expected: " + expected + " got: " + s);
      state = newState;
      barrier.notifyAll();
    }
  }

  /**
   * Awaits the given state. If the state is {@link #DONT_CARE} it will always
   * act as if the state was correct.
   * 
   * @param await The state to wait for.
   */
  protected void awaitState(final int await) {
    if(await == INVALID) throw new IllegalArgumentException("invalid not allowed");
    synchronized(barrier) {
      while((state & await) != state) {
        assert state != INVALID;
        try {
          barrier.wait();
        } catch(final InterruptedException e) {
          Thread.currentThread().interrupt();
          state = DONT_CARE;
        }
      }
    }
  }

  /** The duration of the last drawing phase. */
  protected AtomicLong drawing = new AtomicLong();

  /** The duration of the last animation phase. */
  protected AtomicLong animating = new AtomicLong();

  /**
   * A close block should be used as a resource in code:
   * 
   * <pre>
   * try (CloseBlock b = ...) {
   *   // do stuff...
   * }
   * </pre>
   * 
   * The state of the animation barrier is set after the block has been exited.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  public final class CloseBlock implements AutoCloseable {

    /** The timer to measure how long the block was open. */
    private final Stopwatch timer;
    /** The object to store the duration in. */
    private final AtomicLong duration;
    /** The expected previous state. */
    private final int stateBefore;
    /** The state to be set after the block closes. */
    private int stateAfter;

    /**
     * Creates a block.
     * 
     * @param stateAfter The state to set after the block closes.
     * @param stateBefore The state that is expected.
     * @param duration The object to store the duration of the block in.
     */
    CloseBlock(final int stateAfter, final int stateBefore, final AtomicLong duration) {
      this.stateAfter = stateAfter;
      this.stateBefore = stateBefore;
      this.duration = duration;
      timer = new Stopwatch();
    }

    /** Checks whether the block is already closed. */
    private void ensureValid() {
      if(stateAfter == INVALID) throw new IllegalStateException("block already closed");
    }

    /**
     * Setter.
     * 
     * @param stateAfter Overwrites the state to be set after closing the block.
     */
    public void setStateAfter(final int stateAfter) {
      if(stateAfter == INVALID) throw new IllegalArgumentException("invalid not allowed");
      ensureValid();
      this.stateAfter = stateAfter;
    }

    @Override
    public void close() {
      ensureValid();
      setState(stateAfter, stateBefore);
      stateAfter = INVALID;
      duration.set(timer.currentNano());
    }

  } // CloseBlock

  /**
   * Opens a block for drawing.
   * 
   * @return The object to be closed after drawing.
   */
  public CloseBlock openDrawBlock() {
    synchronized(barrier) {
      awaitState(ALLOW_ALL | ALLOW_DRAW);
      setState(IN_DRAWING, ALLOW_ALL | ALLOW_DRAW);
      return new CloseBlock(ALLOW_ALL, IN_DRAWING, drawing);
    }
  }

  /**
   * Opens a block for animating.
   * 
   * @return The object to be closed after animating.
   */
  public CloseBlock openAnimationBlock() {
    synchronized(barrier) {
      awaitState(ALLOW_ALL);
      setState(IN_ANIMATION, ALLOW_ALL);
      return new CloseBlock(ALLOW_ALL, IN_ANIMATION, animating);
    }
  }

  /**
   * Getter.
   * 
   * @return The time of the last drawing and animation phase combined in
   *         nano-seconds.
   */
  public long lastCycle() {
    return drawing.get() + animating.get();
  }

  /** Disposes this animation barrier. */
  public void dispose() {
    setState(DONT_CARE, DONT_CARE);
  }

}
