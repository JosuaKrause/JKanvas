package jkanvas.table;

import java.util.Objects;

/**
 * An indirect feature that can be added to a different table without losing the
 * connection to the original feature.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class IndirectFeature extends Feature {

  /** The original feature. */
  private final Feature parent;

  /**
   * Constructs an indirect feature.
   * 
   * @param table The table.
   * @param col The column.
   * @param parent The original feature.
   */
  IndirectFeature(final DataTable table, final int col, final Feature parent) {
    super(table, col);
    this.parent = Objects.requireNonNull(parent);
  }

  /**
   * Getter.
   * 
   * @return The original table.
   */
  public DataTable getOriginalTable() {
    return parent.getTable();
  }

  /**
   * Getter.
   * 
   * @return The original column.
   */
  public int getOriginalColumn() {
    return parent.getColumn();
  }

  /**
   * Getter.
   * 
   * @return The original feature.
   */
  public Feature getOriginalFeature() {
    return parent;
  }

}
