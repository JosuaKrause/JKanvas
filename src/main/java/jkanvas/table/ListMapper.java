package jkanvas.table;

import java.util.Objects;

import jkanvas.animation.GenericPaintList;

/**
 * Maps rows of tables to shapes.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The list of shapes.
 */
public abstract class ListMapper<T extends GenericPaintList<?>> {

  /** The table. */
  private final DataTable table;

  /**
   * Creates a map for the table.
   * 
   * @param table The table.
   */
  public ListMapper(final DataTable table) {
    this.table = Objects.requireNonNull(table);
  }

  /**
   * Getter.
   * 
   * @return Creates a new list.
   */
  protected abstract T createList();

  /**
   * Creates a shape for the given row.
   * 
   * @param list The shape list.
   * @param row The row.
   * @return The index of the new shape.
   */
  protected abstract int createForRow(T list, int row);

  /**
   * Fills the list.
   * 
   * @return The list.
   */
  private T fillList() {
    final T res = createList();
    final int rows = table.rows();
    for(int el = 0; el < rows; ++el) {
      final int i = createForRow(res, el);
      // TODO allow arbitrary mappings
      if(i != el) throw new IllegalStateException(
          "unpredicted index: " + i + " != " + el);
    }
    return res;
  }

  /** The list. */
  private T list;

  /**
   * Getter.
   * 
   * @return The list.
   */
  public T getList() {
    if(list == null) {
      list = fillList();
    }
    return list;
  }

  /**
   * Getter.
   * 
   * @return The table.
   */
  public DataTable getTable() {
    return table;
  }

  /**
   * Getter.
   * 
   * @param row The row.
   * @return The index of the shape in the list.
   */
  public int getIndexForRow(final int row) {
    return row;
  }

  /**
   * Getter.
   * 
   * @param index The index of the shape.
   * @return The row in the table.
   */
  public int getRowForIndex(final int index) {
    return index;
  }

}
