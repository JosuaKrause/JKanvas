package jkanvas.util;

import java.util.BitSet;

import jkanvas.table.Metric;

/**
 * Defines metrics for bit sets.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class BitSetMetrics {

  /** No constructor. */
  private BitSetMetrics() {
    throw new AssertionError();
  }

  /**
   * The Jaccard distance of two sets.
   * <code>(|A &cup; B| - |A &cap; B|) / |A &cup; B|</code>
   */
  // TODO #43 -- Java 8 simplification
  public static final Metric<BitSet> JACCARD = new Metric<BitSet>() {

    @Override
    public double distance(final BitSet a, final BitSet b) {
      final int size = Math.max(a.length(), b.length());
      final BitSet and = new BitSet(size);
      and.or(a);
      and.and(b);
      final BitSet or = new BitSet(size);
      or.or(a);
      or.or(b);
      final double union = or.cardinality();
      final double intersection = and.cardinality();
      return (union - intersection) / union;
    }

  };

  /**
   * The Hamming distance of two sets. <code>|A &oplus; B|</code>
   */
  // TODO #43 -- Java 8 simplification
  public static final Metric<BitSet> HAMMING = new Metric<BitSet>() {

    @Override
    public double distance(final BitSet a, final BitSet b) {
      final int size = Math.max(a.length(), b.length());
      final BitSet hamm = new BitSet(size);
      hamm.or(a);
      hamm.xor(b);
      return hamm.cardinality();
    }

  };

}
