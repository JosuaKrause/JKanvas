package jkanvas.table;

/**
 * A transposed view on the given table.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
class TransposedTable extends WrappedTable {

  /**
   * Creates a transposed view on the given table.
   * 
   * @param table The table.
   */
  public TransposedTable(final DataTable table) {
    super(table);
    transposed = table;
  }

  @Override
  public int rows() {
    return table.cols();
  }

  @Override
  public int cols() {
    return table.rows();
  }

  @Override
  public double getAt(final int row, final int col) {
    return table.getAt(col, row);
  }

}
