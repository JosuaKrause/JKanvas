package jkanvas.table;

import java.util.Arrays;

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
  protected Feature[] features() {
    final Feature[] res = new Feature[cols()];
    for(int i = 0; i < res.length; ++i) {
      res[i] = getFeature(i);
    }
    return res;
  }

  /**
   * Getter.
   * 
   * @return All features.
   */
  public final Feature[] getFeatures() {
    final Feature[] fs = features();
    if(hasCachedFeatures()) return Arrays.copyOf(fs, fs.length);
    return fs;
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
   * @return Returns a cached version of the current snapshot of the table.
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
    MINIMUM {

      @Override
      protected double aggregate(final DataTable table, final int col) {
        double min = Double.POSITIVE_INFINITY;
        for(int i = 0; i < table.rows(); ++i) {
          final double v = table.getAt(i, col);
          if(min > v) {
            min = v;
          }
        }
        return min;
      }

    },
    /** The maximum of the column. */
    MAXIMUM {

      @Override
      protected double aggregate(final DataTable table, final int col) {
        double max = Double.NEGATIVE_INFINITY;
        for(int i = 0; i < table.rows(); ++i) {
          final double v = table.getAt(i, col);
          if(max < v) {
            max = v;
          }
        }
        return max;
      }

    },
    /** The mean value of the column. */
    MEAN {

      @Override
      protected double aggregate(final DataTable table, final int col) {
        final int rows = table.rows();
        double sum = 0;
        for(int i = 0; i < rows; ++i) {
          final double v = table.getAt(i, col);
          sum += v;
        }
        return sum / rows;
      }

    },
    /** The standard deviation. */
    STD_DEVIATION {

      @Override
      protected double aggregate(final DataTable table, final int col) {
        final double mean = MEAN.getValue(table, col);
        final int rows = table.rows();
        double sum = 0;
        for(int i = 0; i < rows; ++i) {
          final double v = table.getAt(i, col);
          sum += (v - mean) * (v - mean);
        }
        return Math.sqrt(sum / rows);
      }

    },

    ; // EOD

    /**
     * Computes the actual aggregation.
     * 
     * @param table The table.
     * @param col The column.
     * @return The aggregated value.
     */
    protected abstract double aggregate(DataTable table, int col);

    /**
     * Computes the aggregation for the given column.
     * 
     * @param table The table.
     * @param col The column.
     * @return The value.
     */
    public double getValue(final DataTable table, final int col) {
      final double c = table.getCachedValue(this, col);
      if(!Double.isNaN(c)) return c;
      final double v = aggregate(table, col);
      table.setCachedValue(this, col, v);
      return v;
    }

  } // ColumnAggregation

  /**
   * Computes the aggregated value for the given column.
   * 
   * @param agg The aggregation function.
   * @param col The column.
   * @return The value.
   */
  public double aggregated(final ColumnAggregation agg, final int col) {
    return agg.getValue(this, col);
  }

  /**
   * Getter.
   * 
   * @param agg The aggregation function.
   * @param col The column.
   * @return The cached aggregated value. {@link Double#NaN} signals a not yet
   *         cached value. This method does not compute aggregations.
   */
  protected double getCachedValue(
      @SuppressWarnings("unused") final ColumnAggregation agg,
      @SuppressWarnings("unused") final int col) {
    return Double.NaN;
  }

  /**
   * Setter.
   * 
   * @param agg The aggregation function.
   * @param col The column.
   * @param v The value. This method is a no-op if {@link #isCaching()} returns
   *          <code>false</code>.
   */
  protected void setCachedValue(
      @SuppressWarnings("unused") final ColumnAggregation agg,
      @SuppressWarnings("unused") final int col,
      @SuppressWarnings("unused") final double v) {
    // nothing to do
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
    final double min = ColumnAggregation.MINIMUM.getValue(this, col);
    final double max = ColumnAggregation.MAXIMUM.getValue(this, col);
    if(min == max) return 0;
    return (getAt(row, col) - min) / (max - min);
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
      final double v = table.getCachedValue(agg, c);
      if(Double.isNaN(v)) {
        continue;
      }
      if(res == null) {
        res = new double[cols];
        Arrays.fill(res, Double.NaN);
      }
      res[c] = v;
    }
    return res;
  }

}
