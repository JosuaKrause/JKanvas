package jkanvas.animation;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;

/**
 * A group animator for a group of two animated objects.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class PairAnimator extends GroupAnimator<Animated> {

  /**
   * Creates a pair of animated objects.
   * 
   * @param a The first animated object.
   * @param b The second animated object.
   */
  public PairAnimator(final Animated a, final Animated b) {
    Objects.requireNonNull(a);
    Objects.requireNonNull(b);
    iter = new Iterable<Animated>() {

      @Override
      public Iterator<Animated> iterator() {
        return new Iterator<Animated>() {

          private int p;

          @Override
          public boolean hasNext() {
            return p < 2;
          }

          @Override
          public Animated next() {
            ++p;
            if(p > 2) throw new NoSuchElementException();
            return p < 2 ? a : b;
          }

          @Override
          public void remove() {
            throw new UnsupportedOperationException();
          }

        };
      }

    };
  }

  /** The iterator over the two animated objects. */
  private final Iterable<Animated> iter;

  @Override
  protected Iterable<Animated> members() {
    return iter;
  }

  @Override
  protected Animated animated(final Animated member) {
    return member;
  }

}
