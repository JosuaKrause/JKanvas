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

  /** Whether to force the use of caching in aggregation functions. */
  public static boolean FORCE_CACHE_ON_AGGREGATES = true;

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
    if(FORCE_CACHE_ON_AGGREGATES) throw new IllegalStateException("must be caching");
  }

  /**
   * Normalize the value at the given position.
   * 
   * @param row The row.
   * @param col The column.
   * @return The value at the given position normalized for the column.
   */
  public double getMinMaxScaled(final int row, final int col) {
    if(FORCE_CACHE_ON_AGGREGATES && !isCaching()) throw new IllegalStateException(
        "must be caching");
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
