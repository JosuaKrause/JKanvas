package jkanvas.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.util.StringDrawer;

public abstract class KanvasButton extends AbstractRenderpass {

  private String text;

  private Color color;

  private Color back;

  public KanvasButton(final String text, final Rectangle2D bbox) {
    this.text = Objects.requireNonNull(text);
    color = Color.BLACK;
    back = Color.getHSBColor(60f / 360f, .1f, .96f);
    setBoundingBox(bbox);
  }

  public void setText(final String text) {
    this.text = text;
  }

  public String getText() {
    return text;
  }

  public void setColor(final Color color) {
    this.color = Objects.requireNonNull(color);
  }

  public Color getColor() {
    return color;
  }

  public void setBack(final Color back) {
    this.back = Objects.requireNonNull(back);
  }

  public Color getBack() {
    return back;
  }

  @Override
  public void setBoundingBox(final Rectangle2D bbox) {
    super.setBoundingBox(Objects.requireNonNull(bbox));
  }

  @Override
  public void draw(final Graphics2D gfx, final KanvasContext ctx) {
    final Rectangle2D bbox = getBoundingBox();
    gfx.setColor(back);
    gfx.fill(bbox);
    gfx.setColor(color);
    StringDrawer.drawInto(gfx, text, bbox);
  }

  @Override
  public boolean click(final Point2D p, final MouseEvent e) {
    onClick();
    return true;
  }

  protected abstract void onClick();

}
