package jkanvas;

import java.awt.geom.Rectangle2D;

/**
 * Restricts the area of a {@link ZoomableUI}.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface RestrictedCanvas {

  /**
   * Returns the bounding rectangle in canvas coordinates.
   * 
   * @return The bounding rectangle or <code>null</code> if the canvas should
   *         temporarily not be restricted.
   */
  Rectangle2D getBoundingRect();

  /**
   * Returns the visible rectangle in component coordinates.
   * 
   * @return The visible rectangle in component coordinates.
   */
  Rectangle2D getComponentView();

}
