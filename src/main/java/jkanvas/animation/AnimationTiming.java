package jkanvas.animation;

import java.util.Objects;

import jkanvas.io.json.JSONElement;
import jkanvas.util.Interpolator;

/**
 * The timing of an animation consists of an {@link Interpolator} and a duration
 * of the animation in milliseconds.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class AnimationTiming {

  /** The {@link Interpolator} of the animation. */
  public final Interpolator pol;

  /** The duration of the animation in milliseconds. */
  public final long duration;

  /** The long animation duration. */
  public static final long TIME_LONG = 2000;

  /** The standard animation duration. */
  public static final long TIME_NORMAL = 1000;

  /** Fast animation duration. */
  public static final long TIME_FAST = 100;

  /**
   * Creates an animation timing.
   * 
   * @param pol The interpolation type.
   * @param duration The duration of the animation in milliseconds.
   */
  public AnimationTiming(final Interpolator pol, final long duration) {
    if(duration < 0) throw new IllegalArgumentException("duration: " + duration);
    this.pol = Objects.requireNonNull(pol);
    this.duration = duration;
  }

  /** No animation. */
  public static final AnimationTiming NO_ANIMATION = new AnimationTiming(
      Interpolator.LINEAR, 0);

  /** Slow and smooth animation. */
  public static final AnimationTiming SLOW = new AnimationTiming(
      Interpolator.QUAD_IN_OUT, TIME_LONG);

  /** Fast and linear animation. */
  public static final AnimationTiming FAST = new AnimationTiming(
      Interpolator.LINEAR, TIME_FAST);

  /** Linear animation with normal duration. */
  public static final AnimationTiming LINEAR = new AnimationTiming(
      Interpolator.LINEAR, TIME_LONG);

  /** Smooth animation with normal duration. */
  public static final AnimationTiming SMOOTH = new AnimationTiming(
      Interpolator.QUAD_IN_OUT, TIME_NORMAL);

  /**
   * Loads an animation timing from a JSON element.
   * 
   * @param el The JSON element.
   * @return The animation timing.
   */
  public static final AnimationTiming loadFromJSON(final JSONElement el) {
    if(el.isString()) {
      switch(el.string()) {
        case "no":
        case "none":
        case "no_animation":
          return NO_ANIMATION;
        case "slow":
          return SLOW;
        case "fast":
          return FAST;
        case "linear":
          return LINEAR;
        case "smooth":
          return SMOOTH;
        default:
          throw new IllegalArgumentException("unknown timing: " + el.string());
      }
    }
    el.expectObject();
    final String pol = el.getString("interpolator", "linear");
    final Interpolator i;
    switch(pol) {
      case "slow":
        i = Interpolator.SLOW_IN_OUT;
        break;
      case "smooth":
        i = Interpolator.SMOOTH;
        break;
      case "linear":
        i = Interpolator.LINEAR;
        break;
      case "quad":
        i = Interpolator.QUAD_IN_OUT;
        break;
      default:
        throw new IllegalArgumentException("unknown interpolator: " + pol);
    }
    final long timing = el.getLong("duration", TIME_NORMAL);
    return new AnimationTiming(i, timing);
  }

}
