package jkanvas.painter;

/**
 * An abstract implementation of a {@link Renderpass}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public abstract class AbstractRenderpass extends RenderpassAdapter {

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

  /** The parent. */
  private Renderpass parent;

  /**
   * Setter.
   * 
   * @param parent Sets the parent of this render pass. Parents can not be
   *          directly switched.
   */
  public void setParent(final Renderpass parent) {
    if(parent != null && this.parent != null) throw new IllegalStateException(
        "tried to set two parents");
    this.parent = parent;
  }

  @Override
  public Renderpass getParent() {
    return parent;
  }

  /**
   * {@inheritDoc}
   * <p>
   * This implementation handles the messages "<code>visible:true</code> ", "
   * <code>visible:false</code>", and "<code>visible:toggle</code>".
   */
  @Override
  protected void processMessage(final String msg) {
    super.processMessage(msg);
    switch(msg) {
      case "visible:true":
        setVisible(true);
        break;
      case "visible:false":
        setVisible(false);
        break;
      case "visible:toggle":
        setVisible(!isVisible());
        break;
    }
  }

}
