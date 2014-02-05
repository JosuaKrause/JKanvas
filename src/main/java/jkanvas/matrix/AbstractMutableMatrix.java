package jkanvas.matrix;

import java.awt.geom.Rectangle2D;

/**
 * A straight forward implementation of a {@link Matrix}. Only the creation of
 * the typed arrays must be handled by a sub-class.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The content type.
 */
public abstract class AbstractMutableMatrix<T>
    extends AbstractMatrix<T> implements MutableMatrix<T> {

  /** The row names. */
  private final String[] rowNames;

  /** The column names. */
  private final String[] colNames;

  /** The actual matrix. */
  private final T[][] matrix;

  /** The widths of the columns. */
  private final double[] widths;

  /** The heights of the rows. */
  private final double[] heights;

  /** Cache of all bounding boxes. */
  private Rectangle2D[][] bboxes;

  /**
   * Creates a matrix with the given sizes.
   * 
   * @param rows The number of rows.
   * @param cols The number of columns.
   */
  public AbstractMutableMatrix(final int rows, final int cols) {
    rowNames = new String[rows];
    colNames = new String[cols];
    widths = new double[cols];
    heights = new double[rows];
    bboxes = new Rectangle2D[rows][cols];
    matrix = createMatrix(rows, cols);
  }

  /**
   * Creates an array with the given number of rows and columns.
   * 
   * @param rows The number of rows.
   * @param cols The number of columns.
   * @return An array.
   */
  protected abstract T[][] createMatrix(int rows, int cols);

  @Override
  public double getHeight(final int row) {
    return heights[row];
  }

  @Override
  public double getWidth(final int col) {
    return widths[col];
  }

  /** Invalidates the cache. */
  private void invalidateCache() {
    bboxes = new Rectangle2D[heights.length][widths.length];
  }

  @Override
  public void setHeight(final int row, final double value) {
    heights[row] = value;
    invalidateCache();
    refreshAll();
  }

  @Override
  public void setWidth(final int col, final double value) {
    widths[col] = value;
    invalidateCache();
    refreshAll();
  }

  @Override
  public String getRowName(final int row) {
    return rowNames[row] == null ? "" : rowNames[row];
  }

  @Override
  public String getColumnName(final int col) {
    return colNames[col] == null ? "" : colNames[col];
  }

  @Override
  public void setRowName(final int row, final String name) {
    rowNames[row] = name;
    refreshAll();
  }

  @Override
  public void setColumnName(final int col, final String name) {
    colNames[col] = name;
    refreshAll();
  }

  @Override
  public T get(final int row, final int col) {
    return matrix[row][col];
  }

  @Override
  public void set(final int row, final int col, final T value) {
    matrix[row][col] = value;
    refreshAll();
  }

  @Override
  public int rows() {
    return rowNames.length;
  }

  @Override
  public int cols() {
    return colNames.length;
  }

  @Override
  public void getBoundingBox(final Rectangle2D bbox, final int row, final int col) {
    if(bboxes[row][col] == null) {
      bboxes[row][col] = new Rectangle2D.Double();
      super.getBoundingBox(bboxes[row][col], row, col);
    }
    bbox.setFrame(bboxes[row][col]);
  }

}
