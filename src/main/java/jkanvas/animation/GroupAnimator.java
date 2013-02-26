package jkanvas.animation;

/**
 * Animates a group of animated objects.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <T> A type that has corresponding animated objects.
 */
public abstract class GroupAnimator<T> implements Animated {

  /**
   * Getter.
   * 
   * @return A view on the group members.
   */
  protected abstract Iterable<T> members();

  /**
   * Converts a group member into an animated object.
   * 
   * @param member The group member.
   * @return The animated object.
   */
  protected abstract Animated animated(T member);

  @Override
  public boolean animate(final long currentTime) {
    boolean needsRedraw = false;
    for(final T member : members()) {
      needsRedraw |= animated(member).animate(currentTime);
    }
    return needsRedraw;
  }

}
