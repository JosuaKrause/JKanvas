package jkanvas.painter;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.Camera;
import jkanvas.KanvasContext;
import jkanvas.animation.AnimationList;

/**
 * A ghost render pass is an {@link RenderNode} that mimics the behavior of
 * another {@link RenderNode} but at a different location.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The type of entity to mimic.
 */
public class GhostRenderpass<T extends RenderNode> extends RenderNode {

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

  /**
   * Replaces the parent of the entity with this render pass.
   * 
   * @see #end(RenderNode)
   * @return The old parent.
   */
  private RenderNode start() {
    final RenderNode old = entity.getParent();
    if(old == null || old == this) throw new IllegalStateException(
        "entity already in use");
    entity.setParent(null);
    entity.setParent(this);
    return old;
  }

  /**
   * Restores the old parent of the entity. This method must be called even in
   * the event of an exception or otherwise the wrong parent will be kept. Use
   * the following code snippet to delegate calls to the entity.
   * 
   * <pre>
   * final Renderpass old = start();
   * try {
   *   // use entity here
   * } finally {
   *   end(old);
   * }
   * </pre>
   * 
   * @see #start()
   * @param parent The old parent.
   */
  private void end(final RenderNode parent) {
    entity.setParent(null);
    entity.setParent(parent);
  }

  @Override
  public void draw(final Graphics2D g, final KanvasContext ctx) {
    final RenderNode old = start();
    try {
      entity.draw(g, ctx);
    } finally {
      end(old);
    }
  }

  @Override
  public boolean click(final Camera cam, final Point2D p, final MouseEvent e) {
    final RenderNode old = start();
    try {
      return entity.click(cam, p, e);
    } finally {
      end(old);
    }
  }

  @Override
  public boolean doubleClick(final Camera cam, final Point2D p, final MouseEvent e) {
    final RenderNode old = start();
    try {
      return entity.doubleClick(cam, p, e);
    } finally {
      end(old);
    }
  }

  @Override
  public String getTooltip(final Point2D p) {
    final RenderNode old = start();
    try {
      return entity.getTooltip(p);
    } finally {
      end(old);
    }
  }

  @Override
  public boolean moveMouse(final Point2D cur) {
    final RenderNode old = start();
    try {
      return entity.moveMouse(cur);
    } finally {
      end(old);
    }
  }

  @Override
  public boolean acceptDrag(final Point2D p, final MouseEvent e) {
    final RenderNode old = start();
    try {
      return entity.acceptDrag(p, e);
    } finally {
      end(old);
    }
  }

  @Override
  public void drag(final Point2D start, final Point2D cur,
      final double dx, final double dy) {
    final RenderNode old = start();
    try {
      entity.drag(start, cur, dx, dy);
    } finally {
      end(old);
    }
  }

  @Override
  public void endDrag(final Point2D start, final Point2D end,
      final double dx, final double dy) {
    final RenderNode old = start();
    try {
      entity.endDrag(start, end, dx, dy);
    } finally {
      end(old);
    }
  }

  @Override
  public void getBoundingBox(final Rectangle2D bbox) {
    final RenderNode old = start();
    try {
      entity.getBoundingBox(bbox);
    } finally {
      end(old);
    }
  }

  @Override
  public boolean isChanging() {
    final RenderNode old = start();
    try {
      return entity.isChanging();
    } finally {
      end(old);
    }
  }

  @Override
  public void setForceCache(final boolean forceCache) {
    final RenderNode old = start();
    try {
      entity.setForceCache(forceCache);
    } finally {
      end(old);
    }
  }

  @Override
  public boolean isForceCaching() {
    final RenderNode old = start();
    try {
      return entity.isForceCaching();
    } finally {
      end(old);
    }
  }

  @Override
  public void setAnimationList(final AnimationList list) {
    // we do not set the animation list
    // the entity will set it
  }

}
