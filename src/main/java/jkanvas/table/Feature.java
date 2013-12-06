package jkanvas.table;

import static jkanvas.table.ColumnAggregation.*;

import java.util.Objects;

/**
 * A feature is a column of a {@link DataTable}.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class Feature {

  /** The table. */
  private final DataTable table;
  /** The column. */
  private final int col;

  /**
   * Creates a feature.
   * 
   * @param table The table.
   * @param col The column.
   */
  public Feature(final DataTable table, final int col) {
    this.table = Objects.requireNonNull(table);
    if(col < 0 || col >= table.cols()) throw new IndexOutOfBoundsException("" + col);
    this.col = col;
  }

  /**
   * Getter.
   * 
   * @return The column this feature represents.
   */
  public int getColumn() {
    return col;
  }

  /**
   * Getter.
   * 
   * @return The table of this feature.
   */
  public DataTable getTable() {
    return table;
  }

  /**
   * Getter.
   * 
   * @param row The row.
   * @return The element in the given row.
   */
  public double getElement(final int row) {
    return table.getAt(row, col);
  }

  /**
   * Getter.
   * 
   * @return The number of rows.
   */
  public int rows() {
    return table.rows();
  }

  /**
   * Getter.
   * 
   * @param agg The aggregation function.
   * @return The aggregated value.
   */
  public double aggregate(final ColumnAggregation agg) {
    return agg.getValue(getTable(), getColumn());
  }

  /**
   * Getter.
   * 
   * @param agg The aggregation function.
   * @return The cached value.
   */
  double getCachedValue(final ColumnAggregation agg) {
    return getTable().getCachedValue(agg, getColumn());
  }

  /**
   * Setter.
   * 
   * @param agg The aggregation function.
   * @param v The cached value.
   */
  void setCachedValue(final ColumnAggregation agg, final double v) {
    getTable().setCachedValue(agg, getColumn(), v);
  }

  /**
   * An abstract metric of features.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  private static abstract class FeatureMetric implements Metric<Feature> {

    /** Creates a metric. */
    public FeatureMetric() {
      // nothing to do
    }

    @Override
    public double distance(final Feature a, final Feature b) {
      final int rows = a.rows();
      if(rows != b.rows()) throw new IllegalArgumentException(
          "different number of rows: " + rows + " != " + b.rows());
      double sum = 0.0;
      for(int r = 0; r < rows; ++r) {
        sum += dist(a.getElement(r), b.getElement(r), a, b);
      }
      return endSum(sum, a, b);
    }

    /**
     * The operation after the sum is computed.
     * 
     * @param sum The sum.
     * @param fa The first feature.
     * @param fb The second feature.
     * @return The result.
     */
    protected double endSum(final double sum,
        @SuppressWarnings("unused") final Feature fa,
        @SuppressWarnings("unused") final Feature fb) {
      return Math.sqrt(sum);
    }

    /**
     * The distance of the elements of one row.
     * 
     * @param a The first element.
     * @param b The second element.
     * @param fa The first feature.
     * @param fb The second feature.
     * @return The distance.
     */
    protected abstract double dist(double a, double b, Feature fa, Feature fb);

  } // FeatureMetric

  /** The euclidean metric. */
  public static final Metric<Feature> EUCLID = new FeatureMetric() {

    @Override
    protected double dist(final double a, final double b, final Feature fa,
        final Feature fb) {
      return (a - b) * (a - b);
    }

  }; // EUCLID

  /** The scaled metric. */
  public static final Metric<Feature> SCALED = new FeatureMetric() {

    @Override
    protected double dist(final double a, final double b,
        final Feature fa, final Feature fb) {
      final double ra = fa.aggregate(MAXIMUM) - fa.aggregate(MINIMUM);
      final double rb = fb.aggregate(MAXIMUM) - fb.aggregate(MINIMUM);
      if(ra == 0 || rb == 0) return Double.POSITIVE_INFINITY;
      final double sa = (a - fa.aggregate(MINIMUM)) / ra;
      final double sb = (b - fb.aggregate(MINIMUM)) / rb;
      return (sa - sb) * (sa - sb);
    }

  }; // SCALED

  /**
   * A metric based on the Pearson coefficient. It is modified so that a perfect
   * correlation (-1 or 1) is 0 distance and no correlation (0) is the maximum
   * distance of 1.
   */
  public static final Metric<Feature> PEARSON = new FeatureMetric() {

    @Override
    protected double dist(final double a, final double b,
        final Feature fa, final Feature fb) {
      return (a - fa.aggregate(MEAN)) * (b - fb.aggregate(MEAN));
    }

    @Override
    protected double endSum(final double sum, final Feature fa, final Feature fb) {
      return 1 - Math.abs(
          sum / fa.aggregate(STD_DEVIATION) / fb.aggregate(STD_DEVIATION) / fa.rows());
    }

  };

}
