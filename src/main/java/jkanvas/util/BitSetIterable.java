package jkanvas.util;

import java.util.BitSet;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Iterates over the given bit set.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public final class BitSetIterable implements Iterable<Integer> {

  /**
   * The iterator for the given bit set.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  private class BitSetIterator implements Iterator<Integer> {

    /** The current edge position. */
    private int pos;
    /** The current run. */
    private int currentRun;

    /**
     * Creates a bit set iterator.
     * 
     * @param start The start index.
     */
    public BitSetIterator(final int start) {
      pos = set.nextSetBit(start);
      if(pos >= 0) {
        currentRun = set.nextClearBit(pos);
      }
    }

    @Override
    public boolean hasNext() {
      return pos >= 0 && (end < 0 || pos < end);
    }

    @Override
    public Integer next() {
      if(!hasNext()) throw new NoSuchElementException();
      final int ret = pos;
      if(++pos >= currentRun) {
        pos = set.nextSetBit(pos + 1);
        if(pos >= 0) {
          currentRun = set.nextClearBit(pos);
        }
      }
      return ret;
    }

    @Override
    // TODO #43 -- Java 8 simplification
    public void remove() {
      throw new UnsupportedOperationException();
    }

  } // BitSetIterator

  /** The bit set. */
  protected final BitSet set;

  /** The (optional) exclusive end index. */
  protected final int end;

  /** The start index. */
  private final int start;

  /**
   * Creates a bit set iterable.
   * 
   * @param set The bit set.
   */
  public BitSetIterable(final BitSet set) {
    this(set, 0, -1);
  }

  /**
   * Creates a bit set iterable.
   * 
   * @param set The bit set.
   * @param start The start index.
   */
  public BitSetIterable(final BitSet set, final int start) {
    this(set, start, -1);
  }

  /**
   * Creates a bit set iterable.
   * 
   * @param set The bit set.
   * @param start The start index.
   * @param end The exclusive end index.
   */
  public BitSetIterable(final BitSet set, final int start, final int end) {
    this.set = set;
    this.start = start;
    this.end = end;
  }

  @Override
  public Iterator<Integer> iterator() {
    return new BitSetIterator(start);
  }

}
