package jkanvas.table;

import java.awt.Color;
import java.awt.Shape;

import jkanvas.animation.PointList;

/**
 * A factory that creates point lists.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface PointListFactory {

  /**
   * Creates a point list.
   * 
   * @param table The table that will provide the points.
   * @param rows The estimated number of points.
   * @param defaultColor The default fill color.
   * @param defaultBorder The default border color.
   * @return The point list.
   */
  PointList<? extends Shape> createPointList(DataTable table, int rows,
      Color defaultColor, Color defaultBorder);

}
