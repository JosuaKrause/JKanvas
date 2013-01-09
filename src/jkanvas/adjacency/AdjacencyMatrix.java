package jkanvas.adjacency;


public interface AdjacencyMatrix<T> {

  double getHeight(final int row);

  double getWidth(final int col);

  void setHeight(final int row, final double value);

  void setWidth(final int col, final double value);

  String getName(final int row);

  void setName(final int row, final String name);

  T get(final int row, final int col);

  void set(final int row, final int col, final T value);

  int size();

}
