package jkanvas.matrix;

/**
 * A straight forward implementation of a {@link QuadraticMatrix}. Only the
 * creation of the typed arrays must be handled by a sub-class.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The content type.
 */
public abstract class AbstractQuadraticMatrix<T>
    extends AbstractMatrix<T> implements MutableQuadraticMatrix<T> {

  /**
   * Creates a matrix with the given size.
   * 
   * @param size The size.
   */
  public AbstractQuadraticMatrix(final int size) {
    super(size, size);
  }

  @Override
  public int size() {
    return rows();
  }

  @Override
  public String getName(final int row) {
    return super.getRowName(row);
  }

  @Override
  public String[] getNames() {
    return super.getRowNames();
  }

  @Override
  public void setName(final int row, final String name) {
    super.setRowName(row, name);
  }

  @Override
  public final void setRowName(final int row, final String name) {
    setName(row, name);
  }

  @Override
  public final String getRowName(final int row) {
    return getName(row);
  }

  @Override
  public final String[] getRowNames() {
    return getNames();
  }

  @Override
  public final void setColumnName(final int col, final String name) {
    setName(col, name);
  }

  @Override
  public final String getColumnName(final int col) {
    return getName(col);
  }

  @Override
  public final String[] getColumnNames() {
    return getNames();
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
