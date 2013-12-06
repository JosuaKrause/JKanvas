package jkanvas.table;

import java.util.Arrays;
import java.util.Objects;

/**
 * A table where every value is computed once.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class CachedTable extends DataTable {

  /** The number of rows. */
  private final int rows;
  /** The number of columns. */
  private final int cols;
  /** The content. */
  private final double[][] content;

  /**
   * Creates a cached version of the given table.
   * 
   * @param table The table.
   */
  CachedTable(final DataTable table) {
    Objects.requireNonNull(table);
    rows = table.rows();
    cols = table.cols();
    content = new double[rows][cols];
    for(int r = 0; r < rows; ++r) {
      for(int c = 0; c < cols; ++c) {
        content[r][c] = table.getAt(r, c);
      }
    }
    if(table.hasCachedFeatures()) {
      features = table.features();
    }
    for(final ColumnAggregation agg : ColumnAggregation.values()) {
      final double[] arr = getCachedArray(table, agg);
      if(arr == null) {
        continue;
      }
      setCachedArray(agg, arr);
    }
    if(table.transposed != null && table.transposed.isCaching()) {
      transposed = table.transposed;
      // overwrite back-link when we are the better alternative
      final DataTable tt = transposed.transposed;
      if(tt == null || !tt.isCaching()) {
        table.transposed.transposed = this;
      }
    }
  }

  /**
   * Creates a cached table from an array.
   * 
   * @param table The table with <code>table[row][col]</code>.
   */
  CachedTable(final double[][] table) {
    Objects.requireNonNull(table);
    content = new double[table.length][];
    int cols = -1;
    for(int r = 0; r < table.length; ++r) {
      final double[] row = table[r];
      if(cols < 0) {
        cols = row.length;
      } else if(cols != row.length) throw new IllegalArgumentException(
          "inconsistent column count: " + cols + " " + row.length);
      content[r] = row.clone();
    }
    this.cols = cols >= 0 ? cols : 0;
    rows = content.length;
  }

  @Override
  public int rows() {
    return rows;
  }

  @Override
  public int cols() {
    return cols;
  }

  @Override
  public double getAt(final int row, final int col) {
    return content[row][col];
  }

  @Override
  public boolean isCaching() {
    return true;
  }

  /** The feature cache. This value is computed lazily. */
  private Feature[] features;

  @Override
  public Feature getFeature(final int col) {
    return features()[col];
  }

  @Override
  protected Feature[] features() {
    if(features == null) {
      final Feature[] res = new Feature[cols()];
      for(int i = 0; i < res.length; ++i) {
        res[i] = new Feature(this, i);
      }
      features = res;
    }
    return features;
  }

  @Override
  public boolean hasCachedFeatures() {
    return features != null;
  }

  /** The cache for aggregated values. */
  private double[][] aggs;

  /**
   * Setter.
   * 
   * @param agg The column aggregation function.
   * @param arr The array.
   */
  private void setCachedArray(final ColumnAggregation agg, final double[] arr) {
    if(aggs == null) {
      aggs = new double[ColumnAggregation.values().length][];
    }
    aggs[agg.ordinal()] = arr;
  }

  /**
   * Getter.
   * 
   * @param agg The column aggregation function.
   * @return The array.
   */
  private double[] getCacheArray(final ColumnAggregation agg) {
    if(aggs != null) {
      final double[] res = aggs[agg.ordinal()];
      if(res != null) return res;
    }
    final double[] arr = new double[cols];
    Arrays.fill(arr, Double.NaN);
    setCachedArray(agg, arr);
    return arr;
  }

  @Override
  protected double getCachedValue(final ColumnAggregation agg, final int col) {
    if(aggs == null) return Double.NaN;
    final double[] arr = aggs[agg.ordinal()];
    if(arr == null) return Double.NaN;
    return arr[col];
  }

  @Override
  protected void setCachedValue(final ColumnAggregation agg, final int col, final double v) {
    getCacheArray(agg)[col] = v;
  }

}
