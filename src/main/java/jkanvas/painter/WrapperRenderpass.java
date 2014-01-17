package jkanvas.painter;

import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;

import jkanvas.KanvasContext;

/**
 * A wrapper around a render pass without adding features.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The render pass type.
 */
public class WrapperRenderpass<T extends Renderpass> extends Renderpod<T> {

  /**
   * Wraps the given render pass.
   * 
   * @param wrap The render pass.
   */
  public WrapperRenderpass(final T wrap) {
    super(wrap);
  }

  @Override
  protected void addOwnBox(final Rectangle2D bbox) {
    // nothing to add
  }

  @Override
  protected void drawOwn(final Graphics2D g, final KanvasContext ctx) {
    // nothing to draw
  }

}
