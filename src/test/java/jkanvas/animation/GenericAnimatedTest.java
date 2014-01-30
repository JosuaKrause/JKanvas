package jkanvas.animation;

import static org.junit.Assert.*;

import java.util.concurrent.atomic.AtomicInteger;

import jkanvas.util.Interpolator;

import org.junit.Test;

/**
 * Tests {@link GenericAnimated}. Especially the correct behavior of
 * {@link AnimationAction animation actions} and
 * {@link GenericAnimated#beforeAnimation(AnimationTiming, AnimationAction)} is
 * tested.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class GenericAnimatedTest {

  /**
   * An implementation to test
   * {@link #beforeAnimation(AnimationTiming, AnimationAction)}.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  private static final class TestAnimated extends GenericAnimated<Double> {

    /** Creates with an initial value of 0. */
    public TestAnimated() {
      super(0.0);
    }

    /**
     * The number of calls to
     * {@link #beforeAnimation(AnimationTiming, AnimationAction)}.
     */
    private int count;

    @Override
    protected Double interpolate(final Double from, final Double to, final double t) {
      return from * (1 - t) + to * t;
    }

    @Override
    protected AnimationAction beforeAnimation(
        final AnimationTiming timing, final AnimationAction onFinish) {
      ++count;
      return super.beforeAnimation(timing, onFinish);
    }

    /**
     * Getter.
     * 
     * @return The number of calls to
     *         {@link #beforeAnimation(AnimationTiming, AnimationAction)}.
     */
    public int getCount() {
      return count;
    }

  } // TestAnimated

  /**
   * Tests all values.
   * 
   * @param a The generic animated object.
   * @param e The number of finished actions.
   * @param count The expected number of calls to
   *          {@link GenericAnimated#beforeAnimation(AnimationTiming, AnimationAction)}
   *          .
   * @param value The expected current value.
   * @param eCount The expected number of finished actions.
   */
  private static void test(final TestAnimated a, final AtomicInteger e,
      final int count, final double value, final int eCount) {
    assertEquals(count, a.getCount());
    assertEquals(value, a.get(), 0);
    assertEquals(eCount, e.get());
  }

  /**
   * Tests the correct behavior of finishing actions and calls to
   * {@link GenericAnimated#beforeAnimation(AnimationTiming, AnimationAction)}.
   */
  @Test
  public void testAnimationActions() {
    final AnimationTiming timing = new AnimationTiming(Interpolator.LINEAR, 2);
    final AtomicInteger e = new AtomicInteger();
    // TODO #43 -- Java 8 simplification
    final AnimationAction action = new AnimationAction() {

      @Override
      public void animationFinished() {
        e.incrementAndGet();
      }

    };
    final AnimationList list = new AnimationList();
    final TestAnimated a = new TestAnimated();
    list.addAnimated(a);
    test(a, e, 0, 0, 0);
    a.set(1.0);
    test(a, e, 1, 1, 0);
    a.set(0.0, action);
    test(a, e, 2, 0, 0);
    a.startAnimationTo(2.0, timing, action);
    test(a, e, 3, 0, 0);
    list.doAnimate(0);
    test(a, e, 3, 0, 1);
    list.doAnimate(1);
    test(a, e, 3, 1, 1);
    list.doAnimate(2);
    test(a, e, 3, 2, 2);
    a.startAnimationTo(1.0, timing, action);
    test(a, e, 4, 2, 2);
    a.clearAnimation(action);
    test(a, e, 5, 2, 2);
    list.doAnimate(3);
    test(a, e, 5, 2, 4);
    a.startAnimationTo(4.0, timing);
    test(a, e, 6, 2, 4);
    list.doAnimate(4);
    test(a, e, 6, 2, 4);
    list.doAnimate(5);
    test(a, e, 6, 3, 4);
    a.clearAnimation(action);
    test(a, e, 7, 3, 4);
    list.doAnimate(6);
    test(a, e, 7, 3, 5);
    a.clearAnimation();
    test(a, e, 8, 3, 5);
    a.startAnimationTo(10.0, timing, action);
    a.set(5.0, action);
    test(a, e, 10, 5, 5);
    list.doAnimate(7);
    test(a, e, 10, 5, 7);
    a.startAnimationTo(10.0, AnimationTiming.NO_ANIMATION, action);
    test(a, e, 11, 10, 7);
    list.doAnimate(8);
    test(a, e, 11, 10, 8);
    a.set(.0, action);
    test(a, e, 12, 0, 8);
    list.doAnimate(8);
    test(a, e, 12, 0, 9);
    a.startAnimationTo(1.0, timing);
    test(a, e, 13, 0, 9);
    assertTrue(a.inAnimation());
    assertEquals(1.0, a.getPredict(), 0.0);
    list.doAnimate(9);
    assertTrue(a.inAnimation());
    list.doAnimate(10);
    list.doAnimate(11);
    test(a, e, 13, 1, 9);
    assertEquals(1.0, a.getPredict(), 0.0);
    assertFalse(a.inAnimation());
  }

  /** Immediate actions in an action should not create a live-lock. */
  @Test
  public void liveLockTest() {
    final TestAnimated animated = new TestAnimated();
    final AnimationAction finish = new AnimationAction() {

      private int count;

      @Override
      public void animationFinished() {
        ++count;
        if(count > 100) {
          fail("livelock detected");
        }
        animated.clearAnimation(this);
      }

    };
    animated.clearAnimation(finish);
    animated.animate(0);
    final AnimationList list = new AnimationList();
    final AnimationAction action = new AnimationAction() {

      private int count;

      @Override
      public void animationFinished() {
        ++count;
        if(count > 100) {
          fail("livelock detected");
        }
        list.scheduleAction(this, 0);
      }

    };
    list.scheduleAction(action, 0);
    list.doAnimate(0);
  }

}
