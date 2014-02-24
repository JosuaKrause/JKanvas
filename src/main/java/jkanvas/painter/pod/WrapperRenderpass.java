package jkanvas.painter.pod;

import java.awt.Graphics2D;
import java.awt.geom.RectangularShape;

import jkanvas.KanvasContext;
import jkanvas.painter.Renderpass;

/**
 * A wrapper around a render pass without adding features.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The render pass type.
 */
public final class WrapperRenderpass<T extends Renderpass> extends Renderpod<T> {

  /**
   * Wraps the given render pass.
   * 
   * @param wrap The render pass.
   */
  public WrapperRenderpass(final T wrap) {
    super(wrap);
  }

  @Override
  protected void addOwnBox(final RectangularShape bbox) {
    // nothing to add
  }

  @Override
  protected void drawOwn(final Graphics2D g, final KanvasContext ctx) {
    // nothing to draw
  }

}
