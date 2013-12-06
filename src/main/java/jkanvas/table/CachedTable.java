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
      features = table.getFeatures();
    }
    for(final ColumnAggregation agg : ColumnAggregation.values()) {
      final double[] arr = getCachedArray(table, agg);
      if(arr == null) {
        continue;
      }
      switch(agg) {
        case MINIMUM:
          mins = arr;
          break;
        case MAXIMUM:
          maxs = arr;
          break;
        case MEAN:
          means = arr;
          break;
        case STD_DEVIATION:
          stddevs = arr;
          break;
      }
    }
    if(table.transposed != null && table.transposed.isCaching()) {
      transposed = table.transposed;
      // overwrite back-link when we are the better alternative
      final DataTable t2 = table.transposed.transposed;
      if(t2 == null || !t2.isCaching()) {
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
    return getFeatures()[col];
  }

  @Override
  public Feature[] getFeatures() {
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

  /** The cache for minimums. */
  private double[] mins;

  @Override
  public double getMin(final int col) {
    if(mins == null) {
      mins = new double[cols];
      Arrays.fill(mins, Double.NaN);
    }
    if(Double.isNaN(mins[col])) {
      mins[col] = super.getMin(col);
    }
    return mins[col];
  }

  /** The cache for maximums. */
  private double[] maxs;

  @Override
  public double getMax(final int col) {
    if(maxs == null) {
      maxs = new double[cols];
      Arrays.fill(maxs, Double.NaN);
    }
    if(Double.isNaN(maxs[col])) {
      maxs[col] = super.getMax(col);
    }
    return maxs[col];
  }

  /** The cache for the mean values. */
  private double[] means;

  @Override
  public double getMean(final int col) {
    if(means == null) {
      means = new double[cols];
      Arrays.fill(means, Double.NaN);
    }
    if(Double.isNaN(means[col])) {
      means[col] = super.getMean(col);
    }
    return means[col];
  }

  /** The cache for the standard deviation values. */
  private double[] stddevs;

  @Override
  public double getStdDeviation(final int col) {
    if(stddevs == null) {
      stddevs = new double[cols];
      Arrays.fill(stddevs, Double.NaN);
    }
    if(Double.isNaN(stddevs[col])) {
      stddevs[col] = super.getStdDeviation(col);
    }
    return stddevs[col];
  }

  @Override
  public boolean hasCachedValue(final ColumnAggregation agg, final int col) {
    switch(agg) {
      case MINIMUM:
        return mins != null && !Double.isNaN(mins[col]);
      case MAXIMUM:
        return maxs != null && !Double.isNaN(maxs[col]);
      case MEAN:
        return means != null && !Double.isNaN(means[col]);
      case STD_DEVIATION:
        return stddevs != null && !Double.isNaN(stddevs[col]);
      default:
        Objects.requireNonNull(agg);
        throw new AssertionError();
    }
  }

}
