package jkanvas.adjacency;

/**
 * A position within an {@link AdjacencyMatrix}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class MatrixPosition {

  /** The row. */
  public final int row;

  /** The column. */
  public final int col;

  /**
   * Creates a position within an {@link AdjacencyMatrix}.
   * 
   * @param row The row.
   * @param col The column.
   */
  public MatrixPosition(final int row, final int col) {
    this.row = row;
    this.col = col;
  }

  @Override
  public boolean equals(final Object obj) {
    if(!(obj instanceof MatrixPosition)) return false;
    final MatrixPosition pos = (MatrixPosition) obj;
    return pos.row == row && pos.col == col;
  }

  @Override
  public int hashCode() {
    return row + 31 * col;
  }

}
