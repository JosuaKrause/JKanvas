package jkanvas.adjacency;

public class MatrixPosition {

  public final int row;

  public final int col;

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
