package jkanvas.painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.util.StringDrawer;

/**
 * Shows borders of render passes. Optional titles can be assigned.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class BorderRenderpass extends HUDRenderpassAdapter {

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
  public void drawHUD(final Graphics2D g, final KanvasContext ctx) {
    g.setStroke(new BasicStroke((float) ctx.toComponentLength(2)));
    final AffineTransform at = ctx.toComponentTransformation();
    for(final Entry<Renderpass, String> e : borders.entrySet()) {
      final Renderpass r = e.getKey();
      if(!RenderpassPainter.isTopLevelVisible(r)) {
        continue;
      }
      final Rectangle2D bbox = RenderpassPainter.getTopLevelBounds(r);
      if(bbox == null) {
        continue;
      }
      g.setColor(border);
      final Rectangle2D box = at.createTransformedShape(bbox).getBounds2D();
      g.draw(box);
      final String title = e.getValue();
      if(title == null) {
        continue;
      }
      g.setColor(text);
      StringDrawer.drawText(g, title,
          new Point2D.Double(box.getCenterX(), box.getMaxY()),
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
