package jkanvas.painter;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.KanvasContext;

/**
 * Shows borders of render passes.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 * @param <T> The innermost wrapped type.
 */
public class BorderRenderpass<T extends Renderpass> extends ThinWrapperRenderpass<T> {

  /** The stroke width. */
  private final double width;
  /** The border color. */
  private final Color border;

  /**
   * Creates a border for the given render pass.
   * 
   * @param pass The render pass.
   */
  public BorderRenderpass(final T pass) {
    this(pass, Color.BLACK, 1.0);
  }

  /**
   * Creates a border for the given render pass.
   * 
   * @param pass The render pass.
   * @param border The border color.
   * @param width The stroke width.
   */
  public BorderRenderpass(final T pass, final Color border, final double width) {
    super(pass);
    this.border = Objects.requireNonNull(border);
    this.width = width;
  }

  /**
   * Creates a border for the given render pass.
   * 
   * @param pass The render pass.
   * @param border The border color.
   * @param width The stroke width.
   */
  public BorderRenderpass(final ThinWrapperRenderpass<T> pass,
      final Color border, final double width) {
    super(pass);
    this.border = Objects.requireNonNull(border);
    this.width = width;
  }

  @Override
  public void drawOwn(final Graphics2D g, final KanvasContext ctx) {
    final Rectangle2D box = new Rectangle2D.Double();
    getInnerBoundingBox(box);
    box.setFrame(box.getX() - width * 0.5, box.getY() - width * 0.5,
        box.getWidth() + width, box.getHeight() + width);
    g.setColor(border);
    g.setStroke(new BasicStroke((float) width));
    g.draw(box);
  }

  @Override
  protected void addOwnBox(final Rectangle2D bbox) {
    bbox.setFrame(bbox.getX() - width, bbox.getY() - width,
        bbox.getWidth() + 2 * width, bbox.getHeight() + 2 * width);
  }

}
