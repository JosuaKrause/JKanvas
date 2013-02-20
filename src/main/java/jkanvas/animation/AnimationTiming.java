package jkanvas.animation;

import java.util.Objects;

import jkanvas.util.Interpolator;

/**
 * The timing of an animation consists of an {@link Interpolator} and a duration
 * of the animation in milliseconds.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class AnimationTiming {

  /** The {@link Interpolator} of the animation. */
  public final Interpolator pol;

  /** The duration of the animation in milliseconds. */
  public final int duration;

  /**
   * Creates an animation timing.
   * 
   * @param pol The interpolation type.
   * @param duration The duration of the animation in milliseconds.
   */
  public AnimationTiming(final Interpolator pol, final int duration) {
    if(duration < 0) throw new IllegalArgumentException("duration: " + duration);
    this.pol = Objects.requireNonNull(pol);
    this.duration = duration;
  }

  /** Slow and smooth animation. */
  public static final AnimationTiming SLOW = new AnimationTiming(
      Interpolator.SMOOTH, AnimatedPosition.LONG);

  /** Fast and linear animation. */
  public static final AnimationTiming FAST = new AnimationTiming(
      Interpolator.LINEAR, AnimatedPosition.FAST);

  /** Smooth animation with normal duration. */
  public static final AnimationTiming SMOOTH = new AnimationTiming(
      Interpolator.SMOOTH, AnimatedPosition.NORMAL);

}
