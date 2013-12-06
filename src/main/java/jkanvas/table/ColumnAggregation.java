package jkanvas.table;

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

}
