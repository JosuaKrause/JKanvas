package jkanvas.painter;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.animation.AnimationList;

/**
 * A ghost render pass is an {@link AbstractRenderpass} that mimics the behavior
 * of another {@link Renderpass} but at a different location.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The type of entity to mimic.
 */
public class GhostRenderpass<T extends Renderpass> extends AbstractRenderpass {

  /** The entity to be ghosted. */
  private final T entity;

  /**
   * Creates a ghost of the given entity.
   * 
   * @param entity The render pass to mimic.
   */
  public GhostRenderpass(final T entity) {
    this.entity = Objects.requireNonNull(entity);
  }

  /**
   * Getter.
   * 
   * @return The entity that is ghosted.
   */
  public T getEntity() {
    return entity;
  }

  @Override
  public void draw(final Graphics2D g, final KanvasContext ctx) {
    entity.draw(g, ctx);
  }

  @Override
  public boolean click(final Point2D p, final MouseEvent e) {
    return entity.click(p, e);
  }

  @Override
  public String getTooltip(final Point2D p) {
    return entity.getTooltip(p);
  }

  @Override
  public boolean moveMouse(final Point2D cur) {
    return entity.moveMouse(cur);
  }

  @Override
  public boolean acceptDrag(final Point2D p, final MouseEvent e) {
    return entity.acceptDrag(p, e);
  }

  @Override
  public void drag(final Point2D start, final Point2D cur,
      final double dx, final double dy) {
    entity.drag(start, cur, dx, dy);
  }

  @Override
  public void endDrag(final Point2D start, final Point2D end,
      final double dx, final double dy) {
    entity.endDrag(start, end, dx, dy);
  }

  @Override
  public Rectangle2D getBoundingBox() {
    return entity.getBoundingBox();
  }

  @Override
  public boolean isChanging() {
    return entity.isChanging();
  }

  @Override
  public void setForceCache(final boolean forceCache) {
    entity.setForceCache(forceCache);
  }

  @Override
  public boolean isForceCaching() {
    return entity.isForceCaching();
  }

  @Override
  public void setAnimationList(final AnimationList list) {
    // we do not set the animation list
    // the entity will set it
  }

}
