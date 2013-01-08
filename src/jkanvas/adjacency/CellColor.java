package jkanvas.adjacency;

import java.awt.Color;

public interface CellColor<T> {

  Color getColorFor(int row, int col, T value, boolean isSelected);

  Color getBorderColorFor(int row, int col, T value, boolean isSelected);

}
