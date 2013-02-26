package jkanvas.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.util.StringDrawer;

/**
 * Shows borders of render-passes. Optional titles can be assigned.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class BorderRenderpass extends RenderpassAdapter {

  /** All render-passes to draw borders. */
  private final Map<Renderpass, String> borders = new HashMap<>();
  /** The border color. */
  private final Color border;
  /** The text color. */
  private final Color text;

  /** Creates a border render-pass. */
  public BorderRenderpass() {
    this(Color.BLACK, Color.BLACK);
  }

  /**
   * Creates a border render-pass.
   * 
   * @param border The border color.
   * @param text The text color.
   */
  public BorderRenderpass(final Color border, final Color text) {
    this.border = Objects.requireNonNull(border);
    this.text = Objects.requireNonNull(text);
  }

  @Override
  public void draw(final Graphics2D gfx, final KanvasContext ctx) {
    for(final Entry<Renderpass, String> e : borders.entrySet()) {
      final Renderpass r = e.getKey();
      if(!r.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = RenderpassPainter.getPassBoundingBox(r);
      if(bbox == null) {
        continue;
      }
      gfx.setColor(border);
      gfx.draw(bbox);
      final String title = e.getValue();
      if(title == null) {
        continue;
      }
      gfx.setColor(text);
      StringDrawer.drawText(gfx, title,
          new Point2D.Double(bbox.getCenterX(), bbox.getMaxY()),
          StringDrawer.CENTER_H, StringDrawer.TOP);
    }
  }

  /**
   * Adds a render-pass.
   * 
   * @param renderpass The render-pass.
   */
  public void add(final Renderpass renderpass) {
    add(renderpass, null);
  }

  /**
   * Adds a render-pass with a title.
   * 
   * @param renderpass The render-pass.
   * @param title The title. May be <code>null</code>.
   */
  public void add(final Renderpass renderpass, final String title) {
    borders.put(Objects.requireNonNull(renderpass), title);
  }

  /**
   * Sets the title of a given render-pass.
   * 
   * @param renderpass The render-pass.
   * @param title The title. May be <code>null</code>.
   */
  public void setTitle(final Renderpass renderpass, final String title) {
    add(renderpass, title);
  }

  /**
   * Removes a render-pass.
   * 
   * @param renderpass The render-pass.
   */
  public void remove(final Renderpass renderpass) {
    borders.remove(renderpass);
  }

}
