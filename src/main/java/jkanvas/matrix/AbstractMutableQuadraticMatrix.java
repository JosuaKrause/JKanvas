package jkanvas.matrix;

/**
 * A straight forward implementation of a {@link QuadraticMatrix}. Only the
 * creation of the typed arrays must be handled by a sub-class.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The content type.
 */
public abstract class AbstractMutableQuadraticMatrix<T>
    extends AbstractMutableMatrix<T> implements MutableQuadraticMatrix<T> {

  /**
   * Creates a matrix with the given size.
   * 
   * @param size The size.
   */
  public AbstractMutableQuadraticMatrix(final int size) {
    super(size, size);
  }

  @Override
  public int size() {
    return rows();
  }

  @Override
  public String getName(final int row) {
    return getRowName(row);
  }

  @Override
  public String[] getNames() {
    return getRowNames();
  }

  @Override
  public void setName(final int row, final String name) {
    // set for rows and columns so they don't get out of sync
    super.setRowName(row, name);
    super.setColumnName(row, name);
  }

  @Override
  public final void setRowName(final int row, final String name) {
    setName(row, name);
  }

  @Override
  public final void setColumnName(final int col, final String name) {
    setName(col, name);
  }

  @Override
  protected final T[][] createMatrix(final int rows, final int cols) {
    return createMatrix(rows);
  }

  /**
   * Creates a quadratic array with the given size.
   * 
   * @param size The size.
   * @return A quadratic array.
   */
  protected abstract T[][] createMatrix(int size);

}
