package jkanvas.groups;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.animation.AnimatedLayouter;
import jkanvas.animation.AnimatedPosition;
import jkanvas.animation.Animator;
import jkanvas.painter.AbstractRenderpass;
import jkanvas.painter.Renderpass;
import jkanvas.painter.RenderpassPainter;

/**
 * A group of render-passes. The layout of the render-passes is determined by
 * subclasses. Render-passes without bounding boxes may or may not be allowed
 * depending of the implementation of the subclass. Transitions between layouts
 * can be animated.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public abstract class RenderGroup extends AbstractRenderpass implements AnimatedLayouter {

  /**
   * The offset of a render-pass as {@link AnimatedPosition}.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  protected static final class RenderpassPosition extends AnimatedPosition {

    /** The render-pass. */
    public final AbstractRenderpass pass;

    /** The current render-pass bounding box. */
    private Rectangle2D bbox;

    /**
     * Creates a render-pass position.
     * 
     * @param pass The render-pass.
     */
    public RenderpassPosition(final AbstractRenderpass pass) {
      super(pass.getOffsetX(), pass.getOffsetY());
      bbox = pass.getBoundingBox();
      this.pass = pass;
    }

    @Override
    protected void doSetPosition(final double x, final double y) {
      super.doSetPosition(x, y);
      pass.setOffset(x, y);
    }

    /**
     * Checks whether the bounding box has changed since the last call and sets
     * the current bounding box.
     * 
     * @return Whether the bounding box has changed.
     */
    public boolean checkBBoxChange() {
      final Rectangle2D oldBBox = bbox;
      bbox = RenderpassPainter.getPassBoundingBox(pass);
      if(bbox == null || oldBBox == null) return bbox != oldBBox;
      return bbox.getWidth() != oldBBox.getWidth()
          || bbox.getHeight() != oldBBox.getHeight();
    }

    /**
     * Getter.
     * 
     * @return The render-pass bounding box. The value is refreshed by calling
     *         {@link #checkBBoxChange()}.
     */
    public Rectangle2D getPassBBox() {
      return bbox;
    }

    /**
     * Getter.
     * 
     * @return When the position is in animation the result is the destination
     *         bounding-box of the render-pass. Otherwise the current bounding
     *         box is returned.
     */
    public Rectangle2D getPredictBBox() {
      final Rectangle2D rect = pass.getBoundingBox();
      if(rect == null) return null;
      return new Rectangle2D.Double(rect.getX() + getPredictX(),
          rect.getY() + getPredictY(), rect.getWidth(), rect.getHeight());
    }

  }

  /** If this flag is set the layout is recomputed when the group is drawn. */
  private boolean redoLayout;

  /** The list of group members. */
  private final List<RenderpassPosition> members = new ArrayList<>();

  private final List<Renderpass> nonLayouted = new ArrayList<>();

  /** The underlying animator. */
  private final Animator animator;

  /**
   * Creates a new render-pass group.
   * 
   * @param animator The underlying animator.
   */
  public RenderGroup(final Animator animator) {
    this.animator = Objects.requireNonNull(animator);
  }

  /**
   * Converts a render-pass to a render-pass position.
   * 
   * @param pass The render-pass.
   * @return The position.
   */
  private RenderpassPosition convert(final AbstractRenderpass pass) {
    if(this == pass) throw new IllegalArgumentException("cannot add itself");
    Objects.requireNonNull(pass);
    return new RenderpassPosition(pass);
  }

  /**
   * This method is called after a render-pass is added.
   * 
   * @param _ The render-pass position.
   */
  protected void addedRenderpass(@SuppressWarnings("unused") final RenderpassPosition _) {
    // nothing to do
  }

  /**
   * This method is called after a render-pass is removed.
   * 
   * @param _ The render-pass position.
   */
  protected void removedRenderpass(@SuppressWarnings("unused") final RenderpassPosition _) {
    // nothing to do
  }

  /**
   * Adds a render-pass.
   * 
   * @param pass The render-pass.
   */
  public void addRenderpass(final AbstractRenderpass pass) {
    final RenderpassPosition p = convert(pass);
    members.add(p);
    addedRenderpass(p);
    invalidate();
  }

  /**
   * Inserts a render-pass.
   * 
   * @param index The index where the render-pass will be inserted.
   * @param pass The render-pass.
   */
  public void addRenderpass(final int index, final AbstractRenderpass pass) {
    final RenderpassPosition p = convert(pass);
    members.add(index, p);
    addedRenderpass(p);
    invalidate();
  }

  /**
   * Removes the render-pass at the given index.
   * 
   * @param index The index.
   */
  public void removeRenderpass(final int index) {
    final RenderpassPosition p = members.remove(index);
    removedRenderpass(p);
    invalidate();
  }

  /** Clears all render-passes. */
  public void clearRenderpasses() {
    final RenderpassPosition[] rps = members();
    members.clear();
    for(final RenderpassPosition p : rps) {
      removedRenderpass(p);
    }
    invalidate();
  }

  public void addNonLayouted(final Renderpass pass) {
    nonLayouted.add(pass);
    animator.quickRefresh();
  }

  /**
   * Getter.
   * 
   * @return The number of render-passes.
   */
  public int renderpassCount() {
    return members.size();
  }

  /**
   * Getter.
   * 
   * @param index The index.
   * @return The render-pass at the given position.
   */
  public AbstractRenderpass getRenderpass(final int index) {
    return members.get(index).pass;
  }

  /**
   * Setter.
   * 
   * @param index The index.
   * @param pass The render-pass.
   */
  public void setRenderpass(final int index, final AbstractRenderpass pass) {
    final RenderpassPosition p = convert(pass);
    final RenderpassPosition o = members.set(index, p);
    removedRenderpass(o);
    addedRenderpass(p);
    invalidate();
  }

  /** Invalidates the current layout, recomputes the layout, and repaints. */
  protected void invalidate() {
    redoLayout = true;
    animator.quickRefresh();
  }

  /**
   * Computes the current layout. The implementation may decide whether to allow
   * render-passes without bounding box.
   * 
   * @param members The positions of the render-passes.
   */
  protected abstract void doLayout(List<RenderpassPosition> members);

  @Override
  public Iterable<? extends AnimatedPosition> getPositions() {
    return members;
  }

  /** Immediately computes the current layout. */
  public void forceLayout() {
    redoLayout = false;
    final int oldHash = members.hashCode();
    // doLayout(...) is allowed to call invalidate() and alter members
    doLayout(members);
    // heuristic for modification check
    if(members.hashCode() != oldHash) {
      redoLayout = true;
    }
    animator.forceNextFrame();
  }

  @Override
  public void draw(final Graphics2D gfx, final KanvasContext ctx) {
    if(redoLayout) {
      forceLayout();
    }
    final Rectangle2D view = ctx.getVisibleCanvas();
    boolean changed = false;
    for(final RenderpassPosition p : members) {
      final Renderpass r = p.pass;
      if(!r.isVisible()) {
        continue;
      }
      if(p.checkBBoxChange()) {
        changed = true;
      }
      final Rectangle2D bbox = p.getPassBBox();
      if(bbox != null && !view.intersects(bbox)) {
        continue;
      }
      final Graphics2D g = (Graphics2D) gfx.create();
      if(bbox != null) {
        g.setClip(bbox);
      }
      final double dx = r.getOffsetX();
      final double dy = r.getOffsetY();
      g.translate(dx, dy);
      final KanvasContext c = RenderpassPainter.getContextFor(r, ctx);
      r.draw(g, c);
      g.dispose();
    }
    for(final Renderpass r : nonLayouted) {
      if(!r.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = RenderpassPainter.getPassBoundingBox(r);
      if(bbox != null && !view.intersects(bbox)) {
        continue;
      }
      final Graphics2D g = (Graphics2D) gfx.create();
      if(bbox != null) {
        g.setClip(bbox);
      }
      final double dx = r.getOffsetX();
      final double dy = r.getOffsetY();
      g.translate(dx, dy);
      final KanvasContext c = RenderpassPainter.getContextFor(r, ctx);
      r.draw(g, c);
      g.dispose();
    }
    if(changed) {
      invalidate();
    }
  }

  /**
   * Copies the member list for iterations that allow modifications to the list.
   * 
   * @return The member list as array.
   */
  private RenderpassPosition[] members() {
    return members.toArray(new RenderpassPosition[members.size()]);
  }

  @Override
  public final boolean click(final Point2D position, final MouseEvent e) {
    for(final RenderpassPosition p : members()) {
      final Renderpass r = p.pass;
      if(!r.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = r.getBoundingBox();
      final Point2D pos = RenderpassPainter.getPositionFromCanvas(r, position);
      if(bbox != null && !bbox.contains(pos)) {
        continue;
      }
      if(r.click(pos, e)) return true;
    }
    for(final Renderpass r : nonLayouted) {
      if(!r.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = r.getBoundingBox();
      final Point2D pos = RenderpassPainter.getPositionFromCanvas(r, position);
      if(bbox != null && !bbox.contains(pos)) {
        continue;
      }
      if(r.click(pos, e)) return true;
    }
    return false;
  }

  @Override
  public final String getTooltip(final Point2D position) {
    for(final RenderpassPosition p : members()) {
      final Renderpass r = p.pass;
      if(!r.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = r.getBoundingBox();
      final Point2D pos = RenderpassPainter.getPositionFromCanvas(r, position);
      if(bbox != null && !bbox.contains(pos)) {
        continue;
      }
      final String tooltip = r.getTooltip(pos);
      if(tooltip != null) return tooltip;
    }
    for(final Renderpass r : nonLayouted) {
      if(!r.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = r.getBoundingBox();
      final Point2D pos = RenderpassPainter.getPositionFromCanvas(r, position);
      if(bbox != null && !bbox.contains(pos)) {
        continue;
      }
      final String tooltip = r.getTooltip(pos);
      if(tooltip != null) return tooltip;
    }
    return null;
  }

  @Override
  public final boolean moveMouse(final Point2D cur) {
    for(final RenderpassPosition p : members()) {
      final Renderpass r = p.pass;
      if(!r.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = r.getBoundingBox();
      final Point2D pos = RenderpassPainter.getPositionFromCanvas(r, cur);
      if(bbox != null && !bbox.contains(pos)) {
        continue;
      }
      if(r.moveMouse(pos)) return true;
    }
    for(final Renderpass r : nonLayouted) {
      if(!r.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = r.getBoundingBox();
      final Point2D pos = RenderpassPainter.getPositionFromCanvas(r, cur);
      if(bbox != null && !bbox.contains(pos)) {
        continue;
      }
      if(r.moveMouse(pos)) return true;
    }
    return false;
  }

  /** The render-pass currently responsible for dragging. */
  private Renderpass dragging = null;

  /** The start position of the drag in the render-pass coordinates. */
  private Point2D start = null;

  @Override
  public final boolean acceptDrag(final Point2D position, final MouseEvent e) {
    for(final RenderpassPosition p : members()) {
      final Renderpass r = p.pass;
      if(!r.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = r.getBoundingBox();
      final Point2D pos = RenderpassPainter.getPositionFromCanvas(r, position);
      if(bbox != null && !bbox.contains(pos)) {
        continue;
      }
      if(r.acceptDrag(pos, e)) {
        start = pos;
        dragging = r;
        return true;
      }
    }
    for(final Renderpass r : nonLayouted) {
      if(!r.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = r.getBoundingBox();
      final Point2D pos = RenderpassPainter.getPositionFromCanvas(r, position);
      if(bbox != null && !bbox.contains(pos)) {
        continue;
      }
      if(r.acceptDrag(pos, e)) {
        start = pos;
        dragging = r;
        return true;
      }
    }
    return false;
  }

  @Override
  public final void drag(final Point2D _, final Point2D cur,
      final double dx, final double dy) {
    if(dragging == null) return;
    // dx and dy do not change
    final Point2D pos = RenderpassPainter.getPositionFromCanvas(dragging, cur);
    dragging.drag(start, pos, dx, dy);
  }

  @Override
  public final void endDrag(final Point2D _, final Point2D end,
      final double dx, final double dy) {
    if(dragging == null) return;
    // dx and dy do not change
    final Point2D pos = RenderpassPainter.getPositionFromCanvas(dragging, end);
    dragging.endDrag(start, pos, dx, dy);
    dragging = null;
  }

  @Override
  public void setBoundingBox(final Rectangle2D bbox) {
    // own bounding box calculation
    throw new UnsupportedOperationException();
  }

  @Override
  public Rectangle2D getBoundingBox() {
    boolean change = false;
    Rectangle2D res = null;
    for(final RenderpassPosition p : members) {
      if(!p.pass.isVisible()) {
        continue;
      }
      if(p.checkBBoxChange()) {
        change = true;
      }
      final Rectangle2D bbox = p.getPassBBox();
      if(bbox == null) {
        continue;
      }
      if(res == null) {
        res = new Rectangle2D.Double(bbox.getX(), bbox.getY(),
            bbox.getWidth(), bbox.getHeight());
      } else {
        res.add(bbox);
      }
      if(p.inAnimation()) {
        res.add(p.getPredictBBox());
      }
    }
    for(final Renderpass r : nonLayouted) {
      if(!r.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = RenderpassPainter.getPassBoundingBox(r);
      if(bbox == null) {
        continue;
      }
      if(res == null) {
        res = new Rectangle2D.Double(bbox.getX(), bbox.getY(),
            bbox.getWidth(), bbox.getHeight());
      } else {
        res.add(bbox);
      }
    }
    if(change) {
      invalidate();
    }
    return res;
  }

}
