package jkanvas.painter.pod;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;

import jkanvas.KanvasContext;
import jkanvas.painter.Renderpass;
import jkanvas.util.PaintUtil;

/**
 * A card like render pass container.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The contained render pass.
 */
public class CardPod<T extends Renderpass> extends Renderpod<T> {

  /** The group name of the card. */
  private String group;
  /** Whether colors should be subtle. */
  private boolean subtle;
  /** Whether the card is moveable. */
  private boolean moveable = true;
  /** The border of the card. */
  private double border = 5.0;
  /** The stroke width of the border. */
  private double stroke = 1.0;

  /**
   * Creates a card pod.
   * 
   * @param rp The render pass to wrap.
   */
  public CardPod(final T rp) {
    super(rp);
  }

  /**
   * Creates a card pod.
   * 
   * @param rp The render pod to wrap.
   */
  public CardPod(final Renderpod<T> rp) {
    super(rp);
  }

  /**
   * Setter.
   * 
   * @param group The group name of the card or <code>null</code>.
   */
  public void setGroup(final String group) {
    this.group = group;
  }

  /**
   * Getter.
   * 
   * @return The group name of the card or <code>null</code>.
   */
  public String getGroup() {
    return group;
  }

  /**
   * Setter.
   * 
   * @param border The width of the border.
   */
  public void setBorder(final double border) {
    this.border = border;
    setOffset(-border, -border);
  }

  /**
   * Getter.
   * 
   * @return The width of the border.
   */
  public double getBorder() {
    return border;
  }

  /**
   * Setter.
   * 
   * @param subtle Whether colors are subtle.
   */
  public void setSubtle(final boolean subtle) {
    this.subtle = subtle;
  }

  /**
   * Getter.
   * 
   * @return Whether colors are subtle.
   */
  public boolean isSubtle() {
    return subtle;
  }

  /**
   * Setter.
   * 
   * @param stroke The stroke width of the border.
   */
  public void setStrokeWidth(final double stroke) {
    this.stroke = stroke;
  }

  /**
   * Getter.
   * 
   * @return The stroke width of the border.
   */
  public double getStrokeWidth() {
    return stroke;
  }

  /**
   * Setter.
   * 
   * @param moveable Whether the card is moveable.
   */
  public void setMoveable(final boolean moveable) {
    this.moveable = moveable;
  }

  /**
   * Getter.
   * 
   * @return Whether the card is moveable.
   */
  public boolean isMoveable() {
    return moveable;
  }

  /** The background color. */
  private Color back;

  /**
   * Setter.
   * 
   * @param back The background color or <code>null</code>.
   */
  public void setColor(final Color back) {
    this.back = back;
  }

  /**
   * Getter.
   * 
   * @return The background color or <code>null</code>.
   */
  public Color getColor() {
    return back;
  }

  /** Whether to fade the border when zooming. */
  private boolean fadeBorder;

  /**
   * Setter.
   * 
   * @param fadeBorder Whether to fade the border when zooming out.
   */
  public void setFadeBorder(final boolean fadeBorder) {
    this.fadeBorder = fadeBorder;
  }

  /**
   * Getter.
   * 
   * @return Whether to fade the border when zooming out.
   */
  public boolean isFadingBorder() {
    return fadeBorder;
  }

  /** The first drag position. */
  private Point2D firstPos;

  @Override
  public boolean acceptDrag(final Point2D pos, final MouseEvent e) {
    if(!hit(pos) || !isMoveable() || !e.isShiftDown()) {
      firstPos = null;
      return super.acceptDrag(pos, e);
    }
    group = null;
    firstPos = new Point2D.Double(getOffsetX(), getOffsetY());
    return true;
  }

  @Override
  public void drag(final Point2D start, final Point2D cur,
      final double dx, final double dy) {
    if(firstPos == null) {
      super.drag(start, cur, dx, dy);
      return;
    }
    setOffset(firstPos.getX() + dx, firstPos.getY() + dy);
  }

  @Override
  public void endDrag(final Point2D start, final Point2D end,
      final double dx, final double dy) {
    if(firstPos == null) {
      super.endDrag(start, end, dx, dy);
      return;
    }
    drag(start, end, dx, dy);
    firstPos = null;
  }

  /** Whether the whole card should be hit. */
  private boolean hitAll;

  /**
   * Setter.
   * 
   * @param hitAll Whether to hit the whole card.
   */
  public void setHitAll(final boolean hitAll) {
    this.hitAll = hitAll;
  }

  /**
   * Getter.
   * 
   * @return Whether to hit the whole card.
   */
  public boolean isHitAll() {
    return hitAll;
  }

  /**
   * Getter.
   * 
   * @param pos The position.
   * @return Whether the position hits the card.
   */
  public boolean hit(final Point2D pos) {
    final RoundRectangle2D rect = new RoundRectangle2D.Double();
    getRoundRect(rect);
    if(isHitAll()) return rect.contains(pos);
    final Rectangle2D inner = new Rectangle2D.Double();
    getInnerBoundingBox(inner);
    return rect.contains(pos) && !inner.contains(pos);
  }

  @Override
  protected void addOwnBox(final RectangularShape bbox) {
    final double border = getBorder();
    bbox.setFrame(bbox.getX() - border, bbox.getY() - border,
        bbox.getWidth() + 2 * border, bbox.getHeight() + 2 * border);
  }

  /**
   * Getter.
   * 
   * @param rect The rectangle to set.
   */
  protected void getRoundRect(final RoundRectangle2D rect) {
    getBoundingBox(rect);
    PaintUtil.setArc(rect, getBorder());
  }

  @Override
  protected void drawOwn(final Graphics2D g, final KanvasContext ctx) {
    final boolean fb = isFadingBorder();
    final RoundRectangle2D rect = new RoundRectangle2D.Double();
    getRoundRect(rect);
    final double stroke = getStrokeWidth();
    PaintUtil.addPaddingInplace(rect, -stroke * 0.5);
    g.setStroke(new BasicStroke((float) stroke));
    final Color back = getColor();
    if(back != null) {
      if(isSubtle()) {
        final RoundRectangle2D inner = new RoundRectangle2D.Double();
        inner.setRoundRect(rect);
        PaintUtil.addPaddingInplace(inner, -stroke);
        g.setColor(Color.WHITE);
        g.fill(inner);
        if(fb) {
          PaintUtil.drawShape(g, inner, ctx, back, null);
        } else {
          g.setColor(back);
          g.draw(inner);
        }
      } else {
        g.setColor(back);
        g.fill(rect);
      }
    }
    if(fb) {
      PaintUtil.drawShape(g, rect, ctx, Color.BLACK, null);
    } else {
      g.setColor(Color.BLACK);
      g.draw(rect);
    }
  }

}
