package jkanvas.painter;

import java.awt.geom.Rectangle2D;

/**
 * An abstract implementation of a {@link Renderpass}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public abstract class AbstractRenderpass implements Renderpass {

  /** Whether the pass is visible. */
  private boolean isVisible = true;

  @Override
  public boolean isVisible() {
    return isVisible;
  }

  /**
   * Setter. Implementations may override this method with an
   * {@link UnsupportedOperationException} when they provide the value by
   * themselves.
   * 
   * @param isVisible Sets the visibility of this pass.
   */
  public void setVisible(final boolean isVisible) {
    this.isVisible = isVisible;
  }

  /** The x offset in canvas coordinates. */
  private double x;

  /** The y offset in canvas coordinates. */
  private double y;

  /**
   * Setter. Implementations may override this method with an
   * {@link UnsupportedOperationException} when they provide the value by
   * themselves.
   * 
   * @param x Sets the x offset in canvas coordinates.
   * @param y Sets the y offset in canvas coordinates.
   */
  public void setOffset(final double x, final double y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public double getOffsetX() {
    return x;
  }

  @Override
  public double getOffsetY() {
    return y;
  }

  /** The optional bounding box. */
  private Rectangle2D bbox;

  /**
   * Setter. Implementations may override this method with an
   * {@link UnsupportedOperationException} when they provide the value by
   * themselves.
   * 
   * @param bbox Sets the optional bounding box. This method does <em>not</em>
   *          have to account for the offset.
   */
  public void setBoundingBox(final Rectangle2D bbox) {
    this.bbox = bbox;
  }

  @Override
  public Rectangle2D getBoundingBox() {
    return bbox;
  }

}
