package jkanvas.animation;

import static org.junit.Assert.*;

import java.awt.geom.Point2D;

import jkanvas.util.Interpolator;

import org.junit.Test;

/**
 * Tests for animated positions.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class AnimatedPositionTest {

  /** Tests the immediate feed back before a call to animate. */
  @Test
  public void immediateFeedback() {
    final AnimationTiming at = new AnimationTiming(Interpolator.LINEAR, 2);
    final AnimatedPosition p = new AnimatedPosition(3, 4);
    p.setPosition(4, 3);
    assertEquals(4, p.getX(), 0);
    assertEquals(3, p.getY(), 0);
    assertEquals(4, p.getPredictX(), 0);
    assertEquals(3, p.getPredictY(), 0);
    assertFalse(p.inAnimation());
    p.startAnimationTo(new Point2D.Double(6, 5), at);
    assertEquals(4, p.getX(), 0);
    assertEquals(3, p.getY(), 0);
    assertEquals(6, p.getPredictX(), 0);
    assertEquals(5, p.getPredictY(), 0);
    assertTrue(p.inAnimation());
    p.animate(0);
    assertEquals(4, p.getX(), 0);
    assertEquals(3, p.getY(), 0);
    assertEquals(6, p.getPredictX(), 0);
    assertEquals(5, p.getPredictY(), 0);
    assertTrue(p.inAnimation());
    p.animate(1);
    assertEquals(5, p.getX(), 0);
    assertEquals(4, p.getY(), 0);
    assertEquals(6, p.getPredictX(), 0);
    assertEquals(5, p.getPredictY(), 0);
    assertTrue(p.inAnimation());
    p.animate(2);
    assertEquals(6, p.getX(), 0);
    assertEquals(5, p.getY(), 0);
    assertEquals(6, p.getPredictX(), 0);
    assertEquals(5, p.getPredictY(), 0);
    assertFalse(p.inAnimation());
  }

  /**
   * Tests the immediate feed back before a call to animate -- with change
   * animation.
   */
  @Test
  public void immediateFeedbackForChangingAnimations() {
    final AnimationTiming at = new AnimationTiming(Interpolator.LINEAR, 2);
    final AnimatedPosition p = new AnimatedPosition(new Point2D.Double(3, 4));
    p.setPosition(new Point2D.Double(4, 3));
    assertEquals(4, p.getX(), 0);
    assertEquals(3, p.getY(), 0);
    assertEquals(4, p.getPredictX(), 0);
    assertEquals(3, p.getPredictY(), 0);
    assertFalse(p.inAnimation());
    p.startAnimationTo(new Point2D.Double(12, 10), at);
    p.changeAnimationTo(new Point2D.Double(6, 5), at);
    assertEquals(4, p.getX(), 0);
    assertEquals(3, p.getY(), 0);
    assertEquals(6, p.getPredictX(), 0);
    assertEquals(5, p.getPredictY(), 0);
    assertTrue(p.inAnimation());
    p.animate(0);
    assertEquals(4, p.getX(), 0);
    assertEquals(3, p.getY(), 0);
    assertEquals(6, p.getPredictX(), 0);
    assertEquals(5, p.getPredictY(), 0);
    assertTrue(p.inAnimation());
    p.animate(1);
    assertEquals(5, p.getX(), 0);
    assertEquals(4, p.getY(), 0);
    assertEquals(6, p.getPredictX(), 0);
    assertEquals(5, p.getPredictY(), 0);
    assertTrue(p.inAnimation());
    p.animate(2);
    assertEquals(6, p.getX(), 0);
    assertEquals(5, p.getY(), 0);
    assertEquals(6, p.getPredictX(), 0);
    assertEquals(5, p.getPredictY(), 0);
    assertFalse(p.inAnimation());
  }

  /** Clearing an animation before a call to animate. */
  @Test
  public void clearingAnimationsBeforeAnimate() {
    final AnimationTiming at = new AnimationTiming(Interpolator.LINEAR, 2);
    final AnimatedPosition p = new AnimatedPosition(new Point2D.Double(3, 4));
    p.startAnimationTo(new Point2D.Double(4, 3), at);
    assertEquals(3, p.getX(), 0);
    assertEquals(4, p.getY(), 0);
    assertEquals(4, p.getPredictX(), 0);
    assertEquals(3, p.getPredictY(), 0);
    assertTrue(p.inAnimation());
    p.clearAnimation();
    assertFalse(p.inAnimation());
    p.animate(0);
    assertEquals(3, p.getX(), 0);
    assertEquals(4, p.getY(), 0);
    assertEquals(3, p.getPredictX(), 0);
    assertEquals(4, p.getPredictY(), 0);
    assertFalse(p.inAnimation());
    p.animate(1);
    assertEquals(3, p.getX(), 0);
    assertEquals(4, p.getY(), 0);
    assertEquals(3, p.getPredictX(), 0);
    assertEquals(4, p.getPredictY(), 0);
    assertFalse(p.inAnimation());
  }

  /** Clearing an animation after a call to animate. */
  @Test
  public void clearingAnimationsAfterAnimate() {
    final AnimationTiming at = new AnimationTiming(Interpolator.LINEAR, 2);
    final AnimatedPosition p = new AnimatedPosition(new Point2D.Double(3, 4));
    p.startAnimationTo(new Point2D.Double(5, 6), at);
    p.animate(0);
    assertEquals(3, p.getX(), 0);
    assertEquals(4, p.getY(), 0);
    assertEquals(5, p.getPredictX(), 0);
    assertEquals(6, p.getPredictY(), 0);
    assertTrue(p.inAnimation());
    p.animate(1);
    assertEquals(4, p.getX(), 0);
    assertEquals(5, p.getY(), 0);
    assertEquals(5, p.getPredictX(), 0);
    assertEquals(6, p.getPredictY(), 0);
    assertTrue(p.inAnimation());
    p.clearAnimation();
    assertEquals(4, p.getX(), 0);
    assertEquals(5, p.getY(), 0);
    assertEquals(4, p.getPredictX(), 0);
    assertEquals(5, p.getPredictY(), 0);
    assertFalse(p.inAnimation());
    p.animate(2);
    assertEquals(4, p.getX(), 0);
    assertEquals(5, p.getY(), 0);
    assertEquals(4, p.getPredictX(), 0);
    assertEquals(5, p.getPredictY(), 0);
    assertFalse(p.inAnimation());
  }

  /**
   * A call to start animation with no duration should behave exactly like
   * setPosition().
   */
  @Test
  public void actAsNoAnimation() {
    final AnimationTiming at = new AnimationTiming(Interpolator.LINEAR, 0);
    final AnimatedPosition p = new AnimatedPosition(new Point2D.Double(3, 4));
    assertEquals(3, p.getX(), 0);
    assertEquals(4, p.getY(), 0);
    assertEquals(3, p.getPredictX(), 0);
    assertEquals(4, p.getPredictY(), 0);
    assertFalse(p.inAnimation());
    p.startAnimationTo(new Point2D.Double(4, 3), at);
    assertEquals(4, p.getX(), 0);
    assertEquals(3, p.getY(), 0);
    assertEquals(4, p.getPredictX(), 0);
    assertEquals(3, p.getPredictY(), 0);
    assertFalse(p.inAnimation());
    p.animate(0);
    assertEquals(4, p.getX(), 0);
    assertEquals(3, p.getY(), 0);
    assertEquals(4, p.getPredictX(), 0);
    assertEquals(3, p.getPredictY(), 0);
    assertFalse(p.inAnimation());
  }

  /**
   * A call to change animation with no duration should behave exactly like
   * setPosition().
   */
  @Test
  public void actAsNoAnimationChange() {
    final AnimationTiming at = new AnimationTiming(Interpolator.LINEAR, 0);
    final AnimatedPosition p = new AnimatedPosition(new Point2D.Double(3, 4));
    assertEquals(3, p.getX(), 0);
    assertEquals(4, p.getY(), 0);
    assertEquals(3, p.getPredictX(), 0);
    assertEquals(4, p.getPredictY(), 0);
    assertFalse(p.inAnimation());
    p.changeAnimationTo(new Point2D.Double(4, 3), at);
    assertEquals(4, p.getX(), 0);
    assertEquals(3, p.getY(), 0);
    assertEquals(4, p.getPredictX(), 0);
    assertEquals(3, p.getPredictY(), 0);
    assertFalse(p.inAnimation());
    p.animate(0);
    assertEquals(4, p.getX(), 0);
    assertEquals(3, p.getY(), 0);
    assertEquals(4, p.getPredictX(), 0);
    assertEquals(3, p.getPredictY(), 0);
    assertFalse(p.inAnimation());
  }

  /** Checks the behavior of change animation. */
  @Test
  public void correctChangeBehavior() {
    final AnimationTiming at10 = new AnimationTiming(Interpolator.LINEAR, 10);
    final AnimationTiming at2 = new AnimationTiming(Interpolator.LINEAR, 2);
    final AnimatedPosition p = new AnimatedPosition(new Point2D.Double(3, 4));
    assertEquals(3, p.getX(), 0);
    assertEquals(4, p.getY(), 0);
    assertEquals(3, p.getPredictX(), 0);
    assertEquals(4, p.getPredictY(), 0);
    assertFalse(p.inAnimation());
    p.startAnimationTo(new Point2D.Double(5, 6), at2);
    p.animate(0);
    assertEquals(3, p.getX(), 0);
    assertEquals(4, p.getY(), 0);
    assertEquals(5, p.getPredictX(), 0);
    assertEquals(6, p.getPredictY(), 0);
    assertTrue(p.inAnimation());
    p.changeAnimationTo(new Point2D.Double(3, 2), at10);
    assertEquals(3, p.getX(), 0);
    assertEquals(4, p.getY(), 0);
    assertEquals(3, p.getPredictX(), 0);
    assertEquals(2, p.getPredictY(), 0);
    p.animate(1);
    assertEquals(4, p.getX(), 0);
    assertEquals(5, p.getY(), 0);
    assertEquals(3, p.getPredictX(), 0);
    assertEquals(2, p.getPredictY(), 0);
    assertTrue(p.inAnimation());
    p.animate(2);
    assertEquals(3, p.getX(), 0);
    assertEquals(2, p.getY(), 0);
    assertEquals(3, p.getPredictX(), 0);
    assertEquals(2, p.getPredictY(), 0);
    assertFalse(p.inAnimation());
  }

  /**
   * When an animation is changed the new animation timing must not be used even
   * if it has no duration.
   */
  @Test
  public void overtakeChange() {
    final AnimationTiming at0 = new AnimationTiming(Interpolator.LINEAR, 0);
    final AnimationTiming at2 = new AnimationTiming(Interpolator.LINEAR, 2);
    final AnimatedPosition p = new AnimatedPosition(new Point2D.Double(3, 4));
    p.startAnimationTo(new Point2D.Double(5, 6), at2);
    p.animate(0);
    p.changeAnimationTo(new Point2D.Double(0, 0), at0);
    assertEquals(3, p.getX(), 0);
    assertEquals(4, p.getY(), 0);
    assertEquals(0, p.getPredictX(), 0);
    assertEquals(0, p.getPredictY(), 0);
    assertTrue(p.inAnimation());
    p.animate(1);
    assertEquals(4, p.getX(), 0);
    assertEquals(5, p.getY(), 0);
    assertEquals(0, p.getPredictX(), 0);
    assertEquals(0, p.getPredictY(), 0);
    assertTrue(p.inAnimation());
    p.animate(2);
    assertEquals(0, p.getX(), 0);
    assertEquals(0, p.getY(), 0);
    assertEquals(0, p.getPredictX(), 0);
    assertEquals(0, p.getPredictY(), 0);
    assertFalse(p.inAnimation());
  }

  /** Change behaves like start when no animation is present. */
  @Test
  public void changeIsStart() {
    final AnimationTiming at = new AnimationTiming(Interpolator.LINEAR, 2);
    final AnimatedPosition p = new AnimatedPosition(new Point2D.Double(0, 0));
    p.changeAnimationTo(new Point2D.Double(2, 2), at);
    assertEquals(0, p.getX(), 0);
    assertEquals(0, p.getY(), 0);
    assertEquals(2, p.getPredictX(), 0);
    assertEquals(2, p.getPredictY(), 0);
    assertTrue(p.inAnimation());
    p.animate(0);
    assertEquals(0, p.getX(), 0);
    assertEquals(0, p.getY(), 0);
    assertEquals(2, p.getPredictX(), 0);
    assertEquals(2, p.getPredictY(), 0);
    assertTrue(p.inAnimation());
    p.animate(1);
    assertEquals(1, p.getX(), 0);
    assertEquals(1, p.getY(), 0);
    assertEquals(2, p.getPredictX(), 0);
    assertEquals(2, p.getPredictY(), 0);
    assertTrue(p.inAnimation());
    p.animate(2);
    assertEquals(2, p.getX(), 0);
    assertEquals(2, p.getY(), 0);
    assertEquals(2, p.getPredictX(), 0);
    assertEquals(2, p.getPredictY(), 0);
    assertFalse(p.inAnimation());
  }

  /**
   * Using an interpolator that reaches the end position before its time should
   * not cancel the animation.
   */
  @Test
  public void unusualInterpolator() {
    final Interpolator i = new Interpolator() {

      @Override
      public double interpolate(final double t) {
        return (t > .25 && t < .75) ? 0 : 1;
      }

    };
    final AnimationTiming at = new AnimationTiming(i, 5);
    final AnimatedPosition p = new AnimatedPosition(new Point2D.Double(0, 0));
    p.startAnimationTo(new Point2D.Double(1, 1), at);
    assertEquals(0, p.getX(), 0);
    assertEquals(0, p.getY(), 0);
    assertEquals(1, p.getPredictX(), 0);
    assertEquals(1, p.getPredictY(), 0);
    assertTrue(p.inAnimation());
    p.animate(0);
    assertEquals(0, p.getX(), 0);
    assertEquals(0, p.getY(), 0);
    assertEquals(1, p.getPredictX(), 0);
    assertEquals(1, p.getPredictY(), 0);
    assertTrue(p.inAnimation());
    p.animate(1);
    assertEquals(1, p.getX(), 0);
    assertEquals(1, p.getY(), 0);
    assertEquals(1, p.getPredictX(), 0);
    assertEquals(1, p.getPredictY(), 0);
    assertTrue(p.inAnimation());
    p.animate(2);
    assertEquals(0, p.getX(), 0);
    assertEquals(0, p.getY(), 0);
    assertEquals(1, p.getPredictX(), 0);
    assertEquals(1, p.getPredictY(), 0);
    assertTrue(p.inAnimation());
    p.animate(3);
    assertEquals(0, p.getX(), 0);
    assertEquals(0, p.getY(), 0);
    assertEquals(1, p.getPredictX(), 0);
    assertEquals(1, p.getPredictY(), 0);
    assertTrue(p.inAnimation());
    p.animate(4);
    assertEquals(1, p.getX(), 0);
    assertEquals(1, p.getY(), 0);
    assertEquals(1, p.getPredictX(), 0);
    assertEquals(1, p.getPredictY(), 0);
    assertTrue(p.inAnimation());
    p.animate(5);
    assertEquals(1, p.getX(), 0);
    assertEquals(1, p.getY(), 0);
    assertEquals(1, p.getPredictX(), 0);
    assertEquals(1, p.getPredictY(), 0);
    assertFalse(p.inAnimation());
  }

}
