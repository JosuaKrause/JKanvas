package jkanvas.table;

import java.util.Arrays;
import java.util.Objects;

/**
 * Access to tabular data. The size of the table must not change. The contents
 * of the table may change. Use {@link #cached()} for a non-mutable
 * representation of the current snapshot of the table.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public abstract class DataTable {

  /**
   * Getter.
   * 
   * @return The number of rows.
   */
  public abstract int rows();

  /**
   * Getter.
   * 
   * @return The number of columns.
   */
  public abstract int cols();

  /**
   * Getter.
   * 
   * @param row The row.
   * @param col The column.
   * @return The value at the given column.
   */
  public abstract double getAt(final int row, final int col);

  /**
   * Getter.
   * 
   * @param col The column.
   * @return The feature for the given column.
   */
  public Feature getFeature(final int col) {
    return new Feature(this, col);
  }

  /**
   * Getter.
   * 
   * @return All features.
   */
  public Feature[] getFeatures() {
    final Feature[] res = new Feature[cols()];
    for(int i = 0; i < res.length; ++i) {
      res[i] = getFeature(i);
    }
    return res;
  }

  /**
   * Getter.
   * 
   * @return Whether feature objects are cached.
   */
  public boolean hasCachedFeatures() {
    return false;
  }

  /** The cached transposed table. */
  protected DataTable transposed;

  /**
   * Getter.
   * 
   * @return A transposed version of this table.
   */
  public DataTable transposed() {
    if(transposed == null) {
      transposed = new TransposedTable(this);
    }
    return transposed;
  }

  /**
   * Getter.
   * 
   * @return Whether the content of this table is cached. This also applies to
   *         aggregation functions.
   */
  public boolean isCaching() {
    return false;
  }

  /**
   * Getter.
   * 
   * @return Returns a cached verison of the current snapshot of the table.
   *         Aggregations in cached tables are lazily cached.
   */
  public DataTable cached() {
    return new CachedTable(this);
  }

  /**
   * An enumeration of aggregation functions.
   * 
   * @author Joschi <josua.krause@gmail.com>
   */
  public enum ColumnAggregation {
    /** The minimum of the column. */
    MINIMUM,
    /** The maximum of the column. */
    MAXIMUM,
    /** The mean value of the column. */
    MEAN,
    /** The standard deviation. */
    STD_DEVIATION,
    // EOD
    ;

    /**
     * Computes the aggregation for the given feature.
     * 
     * @param f The feature.
     * @return The value.
     */
    public double getValue(final Feature f) {
      return f.getTable().getAggregated(this, f.getColumn());
    }

    /**
     * Computes the aggregation for the given column.
     * 
     * @param table The table.
     * @param col The column.
     * @return The value.
     */
    public double getValue(final DataTable table, final int col) {
      return table.getAggregated(this, col);
    }

  } // ColumnAggregation

  /**
   * Computes the aggregated value for the given column.
   * 
   * @param agg The aggregation function.
   * @param col The column.
   * @return The value.
   */
  public double getAggregated(final ColumnAggregation agg, final int col) {
    switch(agg) {
      case MINIMUM:
        return getMin(col);
      case MAXIMUM:
        return getMax(col);
      case MEAN:
        return getMean(col);
      case STD_DEVIATION:
        return getStdDeviation(col);
      default:
        Objects.requireNonNull(agg);
        throw new AssertionError();
    }
  }

  /**
   * Getter.
   * 
   * @param agg The aggregation function.
   * @param col The column.
   * @return Whether there is a cached value for this aggregation.
   */
  public boolean hasCachedValue(
      @SuppressWarnings("unused") final ColumnAggregation agg,
      @SuppressWarnings("unused") final int col) {
    return false;
  }

  /**
   * Getter.
   * 
   * @param col The column.
   * @return The minimum of the column.
   */
  public double getMin(final int col) {
    double min = Double.POSITIVE_INFINITY;
    for(int i = 0; i < rows(); ++i) {
      final double v = getAt(i, col);
      if(min > v) {
        min = v;
      }
    }
    return min;
  }

  /**
   * Getter.
   * 
   * @param col The column.
   * @return The maximum of the column.
   */
  public double getMax(final int col) {
    double max = Double.NEGATIVE_INFINITY;
    for(int i = 0; i < rows(); ++i) {
      final double v = getAt(i, col);
      if(max < v) {
        max = v;
      }
    }
    return max;
  }

  /**
   * Normalize the value at the given position.
   * 
   * @param row The row.
   * @param col The column.
   * @return The value at the given position normalized for the column.
   */
  public double getMinMaxScaled(final int row, final int col) {
    if(!isCaching()) throw new IllegalStateException("must be caching");
    final double min = getMin(col);
    final double max = getMax(col);
    if(min == max) return 0;
    return (getAt(row, col) - min) / (max - min);
  }

  /**
   * Computes the mean value of the column.
   * 
   * @param col The column.
   * @return The mean.
   */
  public double getMean(final int col) {
    double sum = 0;
    for(int i = 0; i < rows(); ++i) {
      final double v = getAt(i, col);
      sum += v;
    }
    return sum / rows();
  }

  /**
   * Computes the standard deviation of the column.
   * 
   * @param col The column.
   * @return The standard deviation.
   */
  public double getStdDeviation(final int col) {
    final double mean = getMean(col);
    double sum = 0;
    for(int i = 0; i < rows(); ++i) {
      final double v = getAt(i, col);
      sum += (v - mean) * (v - mean);
    }
    return Math.sqrt(sum / rows());
  }

  /**
   * Creates a data table from the given array.
   * 
   * @param table The table with <code>table[row][col]</code>.
   * @return The data table.
   */
  public static final DataTable fromArray(final double[][] table) {
    return new CachedTable(table);
  }

  /**
   * Computes an array of cached values for the given aggregation function.
   * 
   * @param table The table.
   * @param agg The aggregation function.
   * @return The array or <code>null</code> if no value was cached.
   */
  protected static final double[] getCachedArray(
      final DataTable table, final ColumnAggregation agg) {
    double[] res = null;
    final int cols = table.cols();
    for(int c = 0; c < cols; ++c) {
      if(!table.hasCachedValue(agg, c)) {
        continue;
      }
      if(res == null) {
        res = new double[cols];
        Arrays.fill(res, Double.NaN);
      }
      res[c] = table.getAggregated(agg, c);
    }
    return res;
  }

}
