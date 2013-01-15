package jkanvas.painter;

import java.awt.geom.Rectangle2D;

/**
 * An abstract implementation of a {@link Renderpass}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public abstract class AbstractRenderpass implements Renderpass {

  /** Whether this pass is a HUD. */
  private final boolean isHUD;

  /**
   * Creates a render-pass.
   * 
   * @param isHUD Whether the render-pass is a head-up-display.
   */
  public AbstractRenderpass(final boolean isHUD) {
    this.isHUD = isHUD;
  }

  @Override
  public boolean isHUD() {
    return isHUD;
  }

  /** Whether the pass is visible. */
  private boolean isVisible = true;

  @Override
  public boolean isVisible() {
    return isVisible;
  }

  /**
   * Setter.
   * 
   * @param isVisible Sets the visibility of this pass.
   */
  public void setVisibility(final boolean isVisible) {
    this.isVisible = isVisible;
  }

  /** The x offset in canvas coordinates. */
  private double x;

  /** The y offset in canvas coordinates. */
  private double y;

  /**
   * Setter.
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
   * Setter.
   * 
   * @param bbox Sets the optional bounding box. This method does <em>not</em>
   *          have to account for the offset.
   * @throws IllegalStateException If this pass is a HUD.
   */
  public void setBoundingBox(final Rectangle2D bbox) {
    if(isHUD) throw new IllegalStateException("HUDs do not have a bounding box");
    this.bbox = bbox;
  }

  @Override
  public Rectangle2D getBoundingBox() {
    return bbox;
  }

}
