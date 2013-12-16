package jkanvas.table;

/**
 * A transposed view on the given table.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
final class TransposedTable extends WrappedTable {

  /** The column name prefix. */
  private final String prefix;

  /**
   * Creates a transposed view on the given table.
   * 
   * @param table The table.
   */
  public TransposedTable(final DataTable table) {
    super(table);
    transposed = table;
    prefix = generateName();
  }

  @Override
  public boolean isCategorical(final int col) {
    // rows can be a mixture of categorical and numerical data
    return false;
  }

  @Override
  public String getName(final int col) {
    return prefix + col;
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
