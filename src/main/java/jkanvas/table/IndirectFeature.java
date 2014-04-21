package jkanvas.table;

import java.util.Objects;

public class IndirectFeature extends Feature {

  private final Feature parent;

  IndirectFeature(final DataTable table, final int col, final Feature parent) {
    super(table, col);
    this.parent = Objects.requireNonNull(parent);
  }

  public DataTable getOriginalTable() {
    return parent.getTable();
  }

  public int getOriginalColumn() {
    return parent.getColumn();
  }

  public Feature getOriginalFeature() {
    return parent;
  }

}
