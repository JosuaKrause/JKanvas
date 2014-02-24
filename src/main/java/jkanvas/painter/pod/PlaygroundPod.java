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

  private double border = 5.0;

  private double stroke = 1.0;

  public PlaygroundPod(final T rp) {
    super(rp);
  }

  public PlaygroundPod(final Renderpod<T> rp) {
    super(rp);
  }

  public void setBorder(final double border) {
    this.border = border;
    setOffset(-border, -border);
  }

  public double getBorder() {
    return border;
  }

  public void setStrokeWidth(final double stroke) {
    this.stroke = stroke;
  }

  public double getStrokeWidth() {
    return stroke;
  }

  private Color back;

  public void setColor(final Color back) {
    this.back = back;
  }

  public Color getColor() {
    return back;
  }

  private Point2D lastPos;

  @Override
  public boolean acceptDrag(final Point2D pos, final MouseEvent e) {
    if(!hit(pos)) {
      lastPos = null;
      return super.acceptDrag(pos, e);
    }
    lastPos = pos;
    return true;
  }

  @Override
  public void drag(final Point2D start, final Point2D cur,
      final double dx, final double dy) {
    if(lastPos == null) {
      super.drag(start, cur, dx, dy);
      return;
    }
    final double x = cur.getX() - lastPos.getX();
    final double y = cur.getY() - lastPos.getY();
    setOffset(getOffsetX() + x, getOffsetY() + y);
    lastPos = cur;
  }

  @Override
  public void endDrag(final Point2D start, final Point2D end,
      final double dx, final double dy) {
    if(lastPos == null) {
      super.endDrag(start, end, dx, dy);
      return;
    }
    drag(start, end, dx, dy);
    lastPos = null;
  }

  public boolean hit(final Point2D pos) {
    // TODO do the math
    final RoundRectangle2D rect = new RoundRectangle2D.Double();
    getRoundRect(rect);
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

  @Override
  protected void drawOwn(final Graphics2D g, final KanvasContext ctx) {
    final RoundRectangle2D rect = new RoundRectangle2D.Double();
    getRoundRect(rect);
    PaintUtil.addPaddingInplace(rect, -stroke * 0.5);
    g.setStroke(new BasicStroke((float) stroke));
    if(back != null) {
      g.setColor(back);
      g.fill(rect);
    }
    g.setColor(Color.BLACK);
    g.draw(rect);
  }
}
