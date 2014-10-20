package jkanvas.table.bin;

import java.util.Iterator;

import jkanvas.table.ColumnAggregation;
import jkanvas.table.DataTable;
import jkanvas.table.Feature;

/**
 * Aggregates a column into bins.
 *
 * @author Joschi <josua.krause@gmail.com>
 */
public abstract class ColumnBinner {

  /** The overall maximum value. */
  private final double max;
  /** The feature to put in bins. */
  protected final Feature feature;

  /**
   * Creates bins for the given feature.
   *
   * @param feature The feature.
   */
  public ColumnBinner(final Feature feature) {
    this.feature = feature.cached();
    max = feature.aggregate(ColumnAggregation.MAXIMUM);
  }

  /**
   * Getter.
   *
   * @return The total number of bins. This value must always be larger than
   *         zero.
   */
  public abstract int bins();

  /**
   * Getter.
   *
   * @param bin The bin.
   * @return The inclusive minimal value of the given bin. The minima of the
   *         bins must be increasing monotonously.
   */
  protected abstract double minFor(int bin);

  /**
   * Getter.
   *
   * @param bin The bin. When <code>bin == bins()</code>, {@link #getMaxValue()}
   *          is returned.
   * @return The inclusive minimal value of the bin. The minimum of the bins
   *         must be increasing monotonously.
   */
  public final double getMinValueOf(final int bin) {
    final int bins = bins();
    if(bin < 0 || bin > bins) throw new IndexOutOfBoundsException("" + bin);
    if(bin == bins) return getMaxValue();
    return minFor(bin);
  }

  /**
   * Getter.
   *
   * @return The overall maximum value. This value is inclusive.
   */
  public double getMaxValue() {
    return max;
  }

  /**
   * Getter.
   *
   * @param bin The bin.
   * @return The width of the bin.
   */
  public double getWidthOf(final int bin) {
    final int bins = bins();
    if(bin >= bins || bin < 0) throw new IndexOutOfBoundsException("" + bin);
    final double left = getMinValueOf(bin);
    final double right = getMinValueOf(bin + 1);
    return right - left;
  }

  /**
   * Getter.
   *
   * @return The total width of all bins.
   */
  public double getTotalWidth() {
    return getMaxValue() - getMinValueOf(0);
  }

  /**
   * Finds the bin for the given value.
   *
   * @param value The value.
   * @return The first bin the value belongs to.
   */
  public int binFor(final double value) {
    final double max = getMaxValue();
    if(value > max) throw new IllegalArgumentException(
        "value > max: " + value + " > " + max);
    double min = getMinValueOf(0);
    if(value < min) throw new IllegalArgumentException(
        "value < min: " + value + " < " + min);
    final int bins = bins();
    for(int b = 1; b < bins; ++b) {
      min = getMinValueOf(b);
      if(value < min) return b - 1;
    }
    return bins - 1;
  }

  /**
   * Getter.
   *
   * @return Creates an array with the number of elements per bin.
   */
  private int[] getBinCount() {
    if(cache == null) {
      int hc = 0;
      final int[] res = new int[bins()];
      for(final double v : feature) {
        final int count = ++res[binFor(v)];
        if(count > hc) {
          hc = count;
        }
      }
      highestCount = hc;
      cache = res;
    }
    return cache;
  }

  /** The cached bin counts. */
  private int[] cache;
  /** The cached highest bin count. */
  private int highestCount;

  /**
   * Getter.
   *
   * @param bin The bin.
   * @return The number of elements in the bin.
   */
  public int getCountOf(final int bin) {
    return getBinCount()[bin];
  }

  /**
   * Getter.
   *
   * @return The highest number of elements in one bin.
   */
  public int getMaxCount() {
    // ensure highest count is set
    getBinCount();
    return highestCount;
  }

  /** The cache for the entropy. */
  private double entropyCache = Double.NaN;

  /**
   * Getter.
   *
   * @return The entropy in nat
   *         (http://en.wikipedia.org/wiki/Nat_%28information%29).
   */
  public double entropy() {
    if(Double.isNaN(entropyCache)) {
      final double totalCount = feature.rows();
      double e = 0;
      for(final int c : getBinCount()) {
        if(c == 0) {
          continue;
        }
        final double p = c / totalCount;
        e += p * Math.log(p);
      }
      entropyCache = -e;
    }
    return entropyCache;
  }

  /**
   * Getter.
   *
   * @return The feature that is represented.
   */
  public Feature getFeature() {
    return feature;
  }

  /**
   * Creates column bins for the given feature.
   *
   * @param f The feature.
   * @param choice The bin count choice. This will be ignored if the feature is
   *          categorical.
   * @return The column bins.
   */
  public static final ColumnBinner forFeature(final Feature f, final BinChoice choice) {
    final Feature feature = f.cached();
    if(feature.isCategorical()) return new CategoricalBinner(feature);
    return new SimpleBinner(feature, choice.numberOfBins(feature));
  }

  /**
   * Iterates over all rows.
   *
   * @param table The table.
   * @param defaultChoice The default bin choice.
   * @return All rows.
   */
  public static final Iterable<TableRow> binnedRows(
      final DataTable table, final BinChoice defaultChoice) {
    final DataTable t = table.cached();
    final ColumnBinner[] bins = new ColumnBinner[t.cols()];
    for(int c = 0; c < bins.length; ++c) {
      bins[c] = ColumnBinner.forFeature(t.getFeature(c), defaultChoice);
    }
    // bins may not be altered after this point
    return new Iterable<TableRow>() {

      @Override
      public Iterator<TableRow> iterator() {
        return new Iterator<TableRow>() {

          private int r = 0;

          @Override
          public boolean hasNext() {
            return r < t.rows();
          }

          @Override
          public TableRow next() {
            return new TableRow(t, r++, bins);
          }

          @Override
          // TODO #43 -- Java 8 simplification
          public void remove() {
            throw new UnsupportedOperationException();
          }

        };
      }

    };
  }

}
