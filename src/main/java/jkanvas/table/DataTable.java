package jkanvas.table;

/**
 * Access to tabular data. The contents must not change.
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

}
