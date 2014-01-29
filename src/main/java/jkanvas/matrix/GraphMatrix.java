package jkanvas.matrix;

import java.awt.geom.Rectangle2D;

import jkanvas.nodelink.GraphView;

/**
 * An adjacency matrix using a {@link GraphView} as data storage.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The graph view type.
 */
public class GraphMatrix<T extends GraphView> implements QuadraticMatrix<Boolean> {

  /** The underlying view. */
  private final T view;

  /** The size of cells. */
  private final double size;

  /**
   * Creates an adjacency matrix using the given graph view.
   * 
   * @param view The view.
   * @param size The size of cells.
   */
  public GraphMatrix(final T view, final double size) {
    this.view = view;
    this.size = size;
  }

  @Override
  public double getWidth(final int col) {
    return size;
  }

  @Override
  public double getHeight(final int row) {
    return size;
  }

  @Override
  public String getName(final int row) {
    return view.getName(row);
  }

  @Override
  public int size() {
    return view.nodeCount();
  }

  @Override
  public Boolean get(final int row, final int col) {
    return areConnected(row, col);
  }

  /**
   * Getter.
   * 
   * @return Whether the underlying graph is directed.
   */
  public boolean isDirected() {
    return view.isDirected();
  }

  /**
   * Getter.
   * 
   * @param a The edge start id ie the row.
   * @param b The edge end id ie the column.
   * @return Whether there is an edge.
   */
  public boolean areConnected(final int a, final int b) {
    return view.areConnected(a, b);
  }

  /**
   * Getter.
   * 
   * @return The view.
   */
  protected final T getView() {
    return view;
  }

  @Override
  public Rectangle2D getBoundingBox(final int row, final int col) {
    return new Rectangle2D.Double(row * size, col * size, size, size);
  }

}
