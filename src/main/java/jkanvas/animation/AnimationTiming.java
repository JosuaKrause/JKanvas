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

  /** The long animation duration. */
  public static final int TIME_LONG = 2000;

  /** The standard animation duration. */
  public static final int TIME_NORMAL = 1000;

  /** Fast animation duration. */
  public static final int TIME_FAST = 100;

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

  /** No animation. */
  public static final AnimationTiming NO_ANIMATION = new AnimationTiming(
      Interpolator.LINEAR, 0);

  /** Slow and smooth animation. */
  public static final AnimationTiming SLOW = new AnimationTiming(
      Interpolator.SMOOTH, TIME_LONG);

  /** Fast and linear animation. */
  public static final AnimationTiming FAST = new AnimationTiming(
      Interpolator.LINEAR, TIME_FAST);

  /** Smooth animation with normal duration. */
  public static final AnimationTiming SMOOTH = new AnimationTiming(
      Interpolator.SMOOTH, TIME_NORMAL);

}
