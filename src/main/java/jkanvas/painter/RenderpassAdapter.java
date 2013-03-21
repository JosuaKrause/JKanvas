package jkanvas.painter;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import jkanvas.KanvasContext;
import jkanvas.animation.AnimationList;

/**
 * An adapter for render passes.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public abstract class RenderpassAdapter implements Renderpass {

  /** Whether caching is forced. */
  private boolean forceCache;

  @Override
  public void setForceCache(final boolean forceCache) {
    this.forceCache = forceCache;
  }

  @Override
  public boolean isForceCaching() {
    return forceCache;
  }

  @Override
  public void draw(final Graphics2D g, final KanvasContext ctx) {
    // do nothing
  }

  @Override
  public boolean click(final Point2D p, final MouseEvent e) {
    // do nothing when clicking
    return false;
  }

  @Override
  public String getTooltip(final Point2D p) {
    // no tool-tip
    return null;
  }

  @Override
  public boolean acceptDrag(final Point2D p, final MouseEvent e) {
    // no dragging
    return false;
  }

  @Override
  public void drag(final Point2D start, final Point2D cur,
      final double dx, final double dy) {
    // do nothing
  }

  @Override
  public void endDrag(final Point2D start, final Point2D end,
      final double dx, final double dy) {
    drag(start, end, dx, dy);
  }

  @Override
  public boolean moveMouse(final Point2D cur) {
    // do nothing
    return false;
  }

  @Override
  public double getOffsetX() {
    return 0;
  }

  @Override
  public double getOffsetY() {
    return 0;
  }

  @Override
  public Rectangle2D getBoundingBox() {
    // no bbox by default
    return null;
  }

  @Override
  public void setAnimationList(final AnimationList list) {
    // we do not need the animation list
  }

  @Override
  public boolean isVisible() {
    // visible by default
    return true;
  }

  @Override
  public Renderpass getParent() {
    // no parent
    return null;
  }

  @Override
  public boolean isChanging() {
    // be safe and always return true -- TODO is this necessary?
    return true;
  }

  @Override
  public void dispose() {
    // nothing to dispose
  }

}
