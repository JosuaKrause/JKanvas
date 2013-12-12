package jkanvas.table;

/**
 * A column normalized representation of a table.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class NormalizedTable extends CachedTable {

  /**
   * Creates a column normalized version of the given table.
   * 
   * @param table The table.
   */
  public NormalizedTable(final DataTable table) {
    super(table);
    for(int r = 0; r < rows; ++r) {
      final double[] row = content[r];
      for(int c = 0; c < cols; ++c) {
        row[c] = super.getMinMaxScaled(r, c);
      }
    }
    for(int c = 0; c < cols; ++c) {
      setCachedValue(ColumnAggregation.MAXIMUM, c, 1.0);
      setCachedValue(ColumnAggregation.MINIMUM, c, 0.0);
    }
  }

  @Override
  public double getMinMaxScaled(final int row, final int col) {
    return getAt(row, col);
  }

}
