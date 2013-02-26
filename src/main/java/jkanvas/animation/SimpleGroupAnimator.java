package jkanvas.animation;

import java.util.Objects;

/**
 * A fixed group of animated objects.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class SimpleGroupAnimator extends GroupAnimator<Animated> {

  /** The group members. */
  private final Iterable<Animated> members;

  /**
   * Creates a simple group of animated objects.
   * 
   * @param members The group.
   */
  public SimpleGroupAnimator(final Iterable<Animated> members) {
    this.members = Objects.requireNonNull(members);
  }

  @Override
  protected Iterable<Animated> members() {
    return members;
  }

  @Override
  protected Animated animated(final Animated member) {
    return member;
  }

}
