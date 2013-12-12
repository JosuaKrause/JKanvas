package jkanvas.table;

import java.util.BitSet;

import jkanvas.util.BitSetIterable;

/**
 * A view of a table with only a given selection of rows.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class RowSelectionTable extends WrappedTable {

  /** The index map */
  private final int[] indexMap;

  /**
   * Creates a view of the table with no rows with missing values.
   * 
   * @param table The table.
   * @param rows The rows to select.
   */
  public RowSelectionTable(final DataTable table, final BitSet rows) {
    super(table);
    indexMap = new int[rows.cardinality()];
    int pos = 0;
    for(final int i : new BitSetIterable(rows)) {
      indexMap[pos++] = i;
    }
  }

  @Override
  public int rows() {
    return indexMap.length;
  }

  @Override
  public int cols() {
    return table.cols();
  }

  @Override
  public double getAt(final int row, final int col) {
    return table.getAt(indexMap[row], col);
  }

  @Override
  public String getName(final int col) {
    return table.getName(col);
  }

  @Override
  public boolean isCategorical(final int col) {
    return table.isCategorical(col);
  }

}
