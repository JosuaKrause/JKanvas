package jkanvas.table;

import java.util.Arrays;
import java.util.Objects;

/**
 * A table were every value is computed once.
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
  public CachedTable(final DataTable table) {
    Objects.requireNonNull(table);
    rows = table.rows();
    cols = table.cols();
    content = new double[rows][cols];
    for(int r = 0; r < rows; ++r) {
      for(int c = 0; c < cols; ++c) {
        content[r][c] = table.getAt(r, c);
      }
    }
    mins = new double[cols];
    Arrays.fill(mins, Double.NaN);
    maxs = new double[cols];
    Arrays.fill(maxs, Double.NaN);
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

  /** The cache for minimums. */
  private final double[] mins;

  @Override
  public double getMin(final int col) {
    if(Double.isNaN(mins[col])) {
      mins[col] = super.getMin(col);
    }
    return mins[col];
  }

  /** The cache for maximums. */
  private final double[] maxs;

  @Override
  public double getMax(final int col) {
    if(Double.isNaN(maxs[col])) {
      maxs[col] = super.getMax(col);
    }
    return maxs[col];
  }

}
