package jkanvas.table;

import java.util.Objects;

/**
 * Provides a view on another table.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public abstract class WrappedTable extends DataTable {

  /** The original table. */
  protected final DataTable table;

  /**
   * Creates a view on the given table.
   * 
   * @param table The wrapped table.
   */
  public WrappedTable(final DataTable table) {
    this.table = Objects.requireNonNull(table);
  }

}
