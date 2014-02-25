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

public class PlaygroundPod<T extends Renderpass> extends Renderpod<T> {

  private String group;

  private boolean subtle;

  private boolean moveable = true;

  private double border = 5.0;

  private double stroke = 1.0;

  public PlaygroundPod(final T rp) {
    super(rp);
  }

  public PlaygroundPod(final Renderpod<T> rp) {
    super(rp);
  }

  public void setGroup(final String group) {
    this.group = group;
  }

  public String getGroup() {
    return group;
  }

  public void setBorder(final double border) {
    this.border = border;
    setOffset(-border, -border);
  }

  public double getBorder() {
    return border;
  }

  public void setSubtle(final boolean subtle) {
    this.subtle = subtle;
  }

  public boolean isSubtle() {
    return subtle;
  }

  public void setStrokeWidth(final double stroke) {
    this.stroke = stroke;
  }

  public double getStrokeWidth() {
    return stroke;
  }

  public void setMoveable(final boolean moveable) {
    this.moveable = moveable;
  }

  public boolean isMoveable() {
    return moveable;
  }

  private Color back;

  public void setColor(final Color back) {
    this.back = back;
  }

  public Color getColor() {
    return back;
  }

  private Point2D firstPos;

  private boolean hitAll;

  @Override
  public boolean acceptDrag(final Point2D pos, final MouseEvent e) {
    if(!hit(pos) || !moveable) {
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

  public void hitAll(final boolean hitAll) {
    this.hitAll = hitAll;
  }

  public boolean isHitAll() {
    return hitAll;
  }

  public boolean hit(final Point2D pos) {
    final RoundRectangle2D rect = new RoundRectangle2D.Double();
    getRoundRect(rect);
    if(hitAll) return rect.contains(pos);
    final Rectangle2D inner = new Rectangle2D.Double();
    getInnerBoundingBox(inner);
    return rect.contains(pos) && !inner.contains(pos);
  }

  @Override
  protected void addOwnBox(final RectangularShape bbox) {
    bbox.setFrame(bbox.getX() - border, bbox.getY() - border,
        bbox.getWidth() + 2 * border, bbox.getHeight() + 2 * border);
  }

  protected void getRoundRect(final RoundRectangle2D rect) {
    getBoundingBox(rect);
    final double arc = arc();
    rect.setRoundRect(rect.getX(), rect.getY(),
        rect.getWidth(), rect.getHeight(), arc, arc);
  }

  private double arc() {
    return border / (Math.sqrt(2) - 1);
  }

  private double innerArc() {
    return (border - stroke) / (Math.sqrt(2) - 1);
  }

  @Override
  protected void drawOwn(final Graphics2D g, final KanvasContext ctx) {
    final RoundRectangle2D rect = new RoundRectangle2D.Double();
    getRoundRect(rect);
    PaintUtil.addPaddingInplace(rect, -stroke * 0.5);
    g.setStroke(new BasicStroke((float) stroke));
    if(back != null) {
      if(subtle) {
        final double arc = innerArc();
        final RoundRectangle2D inner = new RoundRectangle2D.Double();
        inner.setRoundRect(rect.getX(), rect.getY(),
            rect.getWidth(), rect.getHeight(), arc, arc);
        PaintUtil.addPaddingInplace(inner, -stroke);
        g.setColor(Color.WHITE);
        g.fill(inner);
        g.setColor(back);
        g.draw(inner);
      } else {
        g.setColor(back);
        g.fill(rect);
      }
    }
    g.setColor(Color.BLACK);
    g.draw(rect);
  }

}
