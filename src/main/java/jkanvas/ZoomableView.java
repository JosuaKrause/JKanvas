package jkanvas;

import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

/**
 * A zoom-able user interface that can be translated. Zooming can also be
 * performed.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public interface ZoomableView {

  /**
   * Setter.
   * 
   * @param x The x offset.
   * @param y The y offset.
   */
  void setOffset(final double x, final double y);

  /**
   * Zooms to the on screen (in component coordinates) position.
   * 
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param zooming The amount of zooming.
   */
  // TODO #43 -- Java 8 simplification
  void zoomTicks(final double x, final double y, final double zooming);

  /**
   * Zooms to the on screen (in component coordinates) position.
   * 
   * @param x The x coordinate.
   * @param y The y coordinate.
   * @param factor The factor to alter the zoom level. Must be
   *          <code>&gt;0</code>.
   */
  void zoomTo(final double x, final double y, final double factor);

  /**
   * Zooms towards the center of the given rectangle.
   * 
   * @param factor The zoom factor.
   * @param box The rectangle to zoom to in component coordinates.
   */
  // TODO #43 -- Java 8 simplification
  void zoom(final double factor, final RectangularShape box);

  /**
   * Resets the view such that the origin of canvas coordinates lies in the
   * center of the given rectangle in component coordinates.
   * 
   * @param screen The screen rectangle in component coordinates.
   */
  // TODO #43 -- Java 8 simplification
  void resetView(final RectangularShape screen);

  /**
   * Transforms the view such that the rectangle <code>view</code> in canvas
   * coordinates matches the rectangle <code>screen</code> in component
   * coordinates. An additional margin can be added.
   * 
   * @param view The rectangle in canvas coordinates.
   * @param screen The rectangle in component coordinates.
   * @param margin The margin.
   * @param fit If set to <code>true</code> the <code>view</code> will be
   *          completely visible. When set to <code>false</code> as much as
   *          possible from <code>view</code> will be visible without showing
   *          anything else.
   */
  // TODO #43 -- Java 8 simplification
  void showRectangle(final RectangularShape view, final RectangularShape screen,
      final double margin, final boolean fit);

  /**
   * Getter.
   * 
   * @return the x offset
   */
  double getOffsetX();

  /**
   * Getter.
   * 
   * @return the y offset
   */
  double getOffsetY();

  /**
   * Transforms the given graphics object.
   * 
   * @param g The graphics object.
   */
  // TODO #43 -- Java 8 simplification
  void transform(final Graphics2D g);

  /**
   * Transforms the given affine transformation.
   * 
   * @param at The affine transformation.
   */
  void transform(final AffineTransform at);

  /**
   * Transforms the given affine transformation back.
   * 
   * @param at The affine transformation.
   */
  void transformBack(final AffineTransform at);

  /**
   * Calculates the real coordinate of the given input in component coordinates.
   * 
   * @param s The coordinate in component coordinates. Due to uniform zooming
   *          both horizontal and vertical coordinates can be converted.
   * @return In real coordinates.
   */
  double inReal(final double s);

  /**
   * Calculates the component coordinate of the given input in real coordinates.
   * 
   * @param s The coordinate in real coordinates. Due to uniform zooming both
   *          horizontal and vertical coordinates can be converted.
   * @return In screen coordinates.
   */
  double fromReal(final double s);

  /**
   * Calculates the real coordinate from the components coordinate.
   * 
   * @param x The components x coordinate.
   * @return The real coordinate.
   */
  double getXForScreen(final double x);

  /**
   * Calculates the real coordinate from the components coordinate.
   * 
   * @param y The components y coordinate.
   * @return The real coordinate.
   */
  double getYForScreen(final double y);

  /**
   * Calculates the component coordinate from the real coordinate.
   * 
   * @param x The real x coordinate.
   * @return The component coordinate.
   */
  double getXFromCanvas(final double x);

  /**
   * Calculates the component coordinate from the real coordinate.
   * 
   * @param y The real y coordinate.
   * @return The component coordinate.
   */
  double getYFromCanvas(final double y);

  /**
   * Converts a point in component coordinates to canvas coordinates.
   * 
   * @param p The point.
   * @return The point in the canvas coordinates.
   */
  // TODO #43 -- Java 8 simplification
  Point2D getForScreen(final Point2D p);

  /**
   * Converts a rectangle in component coordinates to canvas coordinates.
   * 
   * @param rect The rectangle.
   * @return The rectangle in canvas coordinates.
   */
  // TODO #43 -- Java 8 simplification
  Rectangle2D toCanvas(final RectangularShape rect);

  /**
   * Getter.
   * 
   * @return Whether a restriction is set.
   */
  boolean isRestricted();

  /**
   * Returns the minimal zoom value.
   * 
   * @return The minimal zoom value. If the value is non-positive then no
   *         restrictions are made.
   */
  double getMinZoom();

  /**
   * Getter.
   * 
   * @return Whether zoom has a minimum.
   */
  boolean hasMinZoom();

  /**
   * Sets the current minimal zoom value.
   * 
   * @param zoom The new minimal zoom value. Non-positive values indicate no
   *          restriction.
   */
  void setMinZoom(final double zoom);

  /**
   * Returns the maximal zoom value.
   * 
   * @return The maximal zoom value. If the value is non-positive then no
   *         restrictions are made.
   */
  double getMaxZoom();

  /**
   * Getter.
   * 
   * @return Whether zoom has a maximum.
   */
  boolean hasMaxZoom();

  /**
   * Sets the current maximal zoom value.
   * 
   * @param zoom The new maximal zoom value. Non-positive values indicate no
   *          restriction.
   */
  void setMaxZoom(final double zoom);

}
