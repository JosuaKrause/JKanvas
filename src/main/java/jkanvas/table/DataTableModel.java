package jkanvas.table;

import java.util.Objects;

import javax.swing.JLabel;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableModel;

/**
 * A simple model for data tables.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class DataTableModel implements TableModel {

  /** The data table to view. */
  private final DataTable table;

  /**
   * Creates a view of the given data table.
   * 
   * @param table The table.
   */
  public DataTableModel(final DataTable table) {
    this.table = Objects.requireNonNull(table);
  }

  @Override
  public void addTableModelListener(final TableModelListener l) {
    // we cannot detect changes
  }

  @Override
  public void removeTableModelListener(final TableModelListener l) {
    // nothing to remove
  }

  @Override
  public Class<?> getColumnClass(final int columnIndex) {
    return JLabel.class;
  }

  @Override
  public int getColumnCount() {
    return table.cols();
  }

  @Override
  public String getColumnName(final int columnIndex) {
    return table.getName(columnIndex);
  }

  @Override
  public int getRowCount() {
    return table.rows();
  }

  @Override
  public Object getValueAt(final int rowIndex, final int columnIndex) {
    final double v = table.getAt(rowIndex, columnIndex);
    return table.isCategorical(columnIndex) ? "value" + v : "" + v;
  }

  @Override
  public boolean isCellEditable(final int rowIndex, final int columnIndex) {
    return false;
  }

  @Override
  public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex) {
    throw new UnsupportedOperationException();
  }

}
