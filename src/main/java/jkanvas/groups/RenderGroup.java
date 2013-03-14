package jkanvas.groups;

import static jkanvas.util.ArrayUtil.*;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.animation.AnimatedPosition;
import jkanvas.animation.AnimationList;
import jkanvas.animation.Animator;
import jkanvas.animation.GenericAnimated;
import jkanvas.painter.AbstractRenderpass;
import jkanvas.painter.Renderpass;
import jkanvas.painter.RenderpassPainter;
import jkanvas.util.PaintUtil;
import jkanvas.util.VecUtil;

/**
 * A group of render passes. The layout of the render passes is determined by
 * subclasses. Render passes without bounding boxes may or may not be allowed
 * depending of the implementation of the subclass. Transitions between layouts
 * can be animated.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public abstract class RenderGroup extends AbstractRenderpass {

  /**
   * The offset of a render pass as {@link AnimatedPosition}.
   * 
   * @author Joschi <josua.krause@googlemail.com>
   */
  protected static final class RenderpassPosition extends GenericAnimated<Point2D> {

    /** The render-pass. */
    public final AbstractRenderpass pass;

    /** The current render-pass bounding box. */
    private Rectangle2D bbox;

    /**
     * Creates a render pass position.
     * 
     * @param pass The render pass.
     * @param list The animation list.
     */
    public RenderpassPosition(final AbstractRenderpass pass, final AnimationList list) {
      super(new Point2D.Double(pass.getOffsetX(), pass.getOffsetY()));
      bbox = pass.getBoundingBox();
      this.pass = pass;
      list.addAnimated(this);
      pass.setAnimationList(list);
    }

    @Override
    protected Point2D interpolate(final Point2D from, final Point2D to, final double t) {
      return VecUtil.interpolate(from, to, t);
    }

    @Override
    protected void doSet(final Point2D t) {
      super.doSet(t);
      pass.setOffset(t.getX(), t.getY());
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
     * @return The render pass bounding box. The value is refreshed by calling
     *         {@link #checkBBoxChange()}.
     */
    public Rectangle2D getPassBBox() {
      return bbox;
    }

    /**
     * Getter.
     * 
     * @return When the position is in animation the result is the destination
     *         bounding-box of the render pass. Otherwise the current bounding
     *         box is returned.
     */
    public Rectangle2D getPredictBBox() {
      final Rectangle2D rect = pass.getBoundingBox();
      if(rect == null) return null;
      final Point2D pred = getPredict();
      return new Rectangle2D.Double(rect.getX() + pred.getX(),
          rect.getY() + pred.getY(), rect.getWidth(), rect.getHeight());
    }

  }

  /** If this flag is set the layout is recomputed when the group is drawn. */
  private boolean redoLayout;

  /** The list of group members. */
  private final List<RenderpassPosition> members;

  /** The list of non layouted members in front of the layouted members. */
  private final List<Renderpass> nlFront = new ArrayList<>();

  /** The list of non layouted members behind the layouted members. */
  private final List<Renderpass> nlBack = new ArrayList<>();

  /** The underlying animator. */
  private final Animator animator;

  /**
   * Creates a new render-pass group.
   * 
   * @param animator The underlying animator.
   */
  public RenderGroup(final Animator animator) {
    final List<RenderpassPosition> m = new ArrayList<>();
    this.animator = Objects.requireNonNull(animator);
    members = m;
    redoLayout = true;
  }

  /**
   * Getter.
   * 
   * @return The animator for this render group.
   */
  public Animator getAnimator() {
    return animator;
  }

  @Override
  public void setAnimationList(final AnimationList list) {
    if(animator.getAnimationList() != list) throw new IllegalArgumentException(
        "attempt to set group to other animation list");
  }

  /**
   * Converts a render pass to a render pass position.
   * 
   * @param pass The render pass.
   * @return The position.
   */
  private RenderpassPosition convert(final AbstractRenderpass pass) {
    if(this == pass) throw new IllegalArgumentException("cannot add itself");
    Objects.requireNonNull(pass);
    return new RenderpassPosition(pass, animator.getAnimationList());
  }

  /**
   * This method is called after a render pass is added.
   * 
   * @param _ The render pass position.
   */
  protected void addedRenderpass(@SuppressWarnings("unused") final RenderpassPosition _) {
    // nothing to do
  }

  /**
   * This method is always called after a render pass is added.
   * 
   * @param p The render pass position.
   */
  private void addedRenderpassIntern(final RenderpassPosition p) {
    p.pass.setParent(this);
    addedRenderpass(p);
  }

  /**
   * This method is called after a render pass is removed.
   * 
   * @param _ The render pass position.
   */
  protected void removedRenderpass(@SuppressWarnings("unused") final RenderpassPosition _) {
    // nothing to do
  }

  /**
   * This method is always called after a render pass is removed.
   * 
   * @param p The render pass position.
   */
  private void removedRenderpassIntern(final RenderpassPosition p) {
    removedRenderpass(p);
    p.pass.setParent(null);
  }

  /**
   * Adds a render pass.
   * 
   * @param pass The render pass.
   */
  public void addRenderpass(final AbstractRenderpass pass) {
    synchronized(animator.getAnimationLock()) {
      final RenderpassPosition p = convert(pass);
      members.add(p);
      addedRenderpassIntern(p);
      invalidate();
    }
  }

  /**
   * Inserts a render-pass.
   * 
   * @param index The index where the render pass will be inserted.
   * @param pass The render pass.
   */
  public void addRenderpass(final int index, final AbstractRenderpass pass) {
    synchronized(animator.getAnimationLock()) {
      final RenderpassPosition p = convert(pass);
      members.add(index, p);
      addedRenderpassIntern(p);
      invalidate();
    }
  }

  /**
   * Getter.
   * 
   * @param r The abstract render pass to find.
   * @return The index of the given render pass or <code>-1</code> if the pass
   *         could not be found. Equality is defined by identity.
   */
  public int indexOf(final AbstractRenderpass r) {
    for(int i = 0; i < members.size(); ++i) {
      final RenderpassPosition rp = members.get(i);
      if(rp.pass == r) return i;
    }
    return -1;
  }

  /**
   * Removes the render pass at the given index.
   * 
   * @param index The index.
   */
  public void removeRenderpass(final int index) {
    synchronized(animator.getAnimationLock()) {
      final RenderpassPosition p = members.remove(index);
      removedRenderpassIntern(p);
      invalidate();
    }
  }

  /** Clears all render passes. */
  public void clearRenderpasses() {
    synchronized(animator.getAnimationLock()) {
      final RenderpassPosition[] rps = members();
      members.clear();
      for(final RenderpassPosition p : rps) {
        removedRenderpassIntern(p);
      }
      invalidate();
    }
  }

  /**
   * Getter.
   * 
   * @return The number of render passes.
   */
  public int renderpassCount() {
    return members.size();
  }

  /**
   * Getter.
   * 
   * @param index The index.
   * @return The render pass at the given position.
   */
  public AbstractRenderpass getRenderpass(final int index) {
    return members.get(index).pass;
  }

  /**
   * Setter.
   * 
   * @param index The index.
   * @param pass The render pass.
   */
  public void setRenderpass(final int index, final AbstractRenderpass pass) {
    synchronized(animator.getAnimationLock()) {
      final RenderpassPosition p = convert(pass);
      final RenderpassPosition o = members.set(index, p);
      removedRenderpassIntern(o);
      addedRenderpassIntern(p);
      invalidate();
    }
  }

  /**
   * Adds a render pass that is not used for the layout.
   * 
   * @param pass The render pass.
   * @param front Whether this pass is added in front of the layouted passes.
   */
  public void addNonLayouted(final Renderpass pass, final boolean front) {
    if(front) {
      nlFront.add(pass);
    } else {
      nlBack.add(pass);
    }
    animator.quickRefresh();
  }

  /**
   * Inserts a render pass that is not used for the layout.
   * 
   * @param index The index where the render-pass will be inserted.
   * @param pass The render pass.
   * @param front Whether this pass is added in front of the layouted passes.
   */
  public void addNonLayouted(final int index, final Renderpass pass, final boolean front) {
    if(front) {
      nlFront.add(index, pass);
    } else {
      nlBack.add(index, pass);
    }
    animator.quickRefresh();
  }

  /**
   * Removes a render pass that is not used for the layout.
   * 
   * @param index The index of the render pass.
   * @param front Whether this pass is found in front of the layouted passes.
   */
  public void removeNonLayouted(final int index, final boolean front) {
    if(front) {
      nlFront.remove(index);
    } else {
      nlBack.remove(index);
    }
    animator.quickRefresh();
  }

  /**
   * Sets the render pass that is not used for the layout at the given position.
   * 
   * @param index The index.
   * @param pass The render pass.
   * @param front Whether this method addresses passes in front of the layouted
   *          passes.
   */
  public void setNonLayouted(final int index, final Renderpass pass, final boolean front) {
    if(front) {
      nlFront.set(index, pass);
    } else {
      nlBack.set(index, pass);
    }
    animator.quickRefresh();
  }

  /**
   * Getter.
   * 
   * @param index The index.
   * @param front Whether this method addresses passes in front of the layouted
   *          passes.
   * @return The render pass at the given index that is not used for the layout.
   */
  public Renderpass getNonLayouted(final int index, final boolean front) {
    return front ? nlFront.get(index) : nlBack.get(index);
  }

  /** Clears all render passes that are not used for the layout. */
  public void clearNonLayouted() {
    nlFront.clear();
    nlBack.clear();
    animator.quickRefresh();
  }

  /**
   * Getter.
   * 
   * @param front Whether this method addresses passes in front of the layouted
   *          passes.
   * @return The number of render passes that are not used for the layout.
   */
  public int nonLayoutedSize(final boolean front) {
    return front ? nlFront.size() : nlBack.size();
  }

  /** Invalidates the current layout, recomputes the layout, and repaints. */
  public void invalidate() {
    redoLayout = true;
    animator.quickRefresh();
  }

  /**
   * Computes the current layout. The implementation may decide whether to allow
   * render passes without bounding box.
   * 
   * @param members The positions of the render passes.
   */
  protected abstract void doLayout(List<RenderpassPosition> members);

  /** Immediately computes the current layout. */
  public void forceLayout() {
    synchronized(animator.getAnimationLock()) {
      redoLayout = false;
      doLayout(Collections.unmodifiableList(members));
      animator.forceNextFrame();
    }
  }

  /** Ensures that the layout is computed. */
  private void ensureLayout() {
    if(!redoLayout) return;
    forceLayout();
  }

  @Override
  public void draw(final Graphics2D gfx, final KanvasContext ctx) {
    ensureLayout();
    gfx.setColor(Color.GREEN);
    RenderpassPainter.draw(nlBack, gfx, ctx);
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
    if(jkanvas.Canvas.DEBUG_BBOX) {
      final Graphics2D g = (Graphics2D) gfx.create();
      PaintUtil.setAlpha(g, 0.3);
      g.setColor(Color.BLUE);
      for(final RenderpassPosition rp : members) {
        final Renderpass r = rp.pass;
        if(!r.isVisible()) {
          continue;
        }
        if(rp.checkBBoxChange()) {
          changed = true;
        }
        final Rectangle2D bbox = rp.getPassBBox();
        if(bbox == null || !view.intersects(bbox)) {
          continue;
        }
        g.fill(bbox);
      }
    }
    gfx.setColor(Color.GREEN);
    RenderpassPainter.draw(nlFront, gfx, ctx);
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
  public boolean click(final Point2D position, final MouseEvent e) {
    if(RenderpassPainter.click(nlFront, position, e)) return true;
    for(final RenderpassPosition p : reverseArray(members())) {
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
    return RenderpassPainter.click(nlBack, position, e);
  }

  @Override
  public String getTooltip(final Point2D position) {
    final String tt = RenderpassPainter.getTooltip(nlFront, position);
    if(tt != null) return tt;
    for(final RenderpassPosition p : reverseArray(members())) {
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
    return RenderpassPainter.getTooltip(nlBack, position);
  }

  @Override
  public boolean moveMouse(final Point2D cur) {
    boolean moved = RenderpassPainter.moveMouse(nlFront, cur);
    for(final RenderpassPosition p : reverseArray(members())) {
      final Renderpass r = p.pass;
      if(!r.isVisible()) {
        continue;
      }
      final Point2D pos = RenderpassPainter.getPositionFromCanvas(r, cur);
      if(r.moveMouse(pos)) {
        moved = true;
      }
    }
    return RenderpassPainter.moveMouse(nlBack, cur) || moved;
  }

  /**
   * Picks a layouted render pass.
   * 
   * @param position The position.
   * @return The render pass at the given position or <code>null</code> if there
   *         is none.
   */
  protected Renderpass pickLayouted(final Point2D position) {
    for(final RenderpassPosition p : reverseArray(members())) {
      final Renderpass r = p.pass;
      if(!r.isVisible()) {
        continue;
      }
      final Rectangle2D bbox = r.getBoundingBox();
      final Point2D pos = RenderpassPainter.getPositionFromCanvas(r, position);
      if(bbox != null && !bbox.contains(pos)) {
        continue;
      }
      return r;
    }
    return null;
  }

  /** The render-pass currently responsible for dragging. */
  private Renderpass dragging = null;

  /** The start position of the drag in the render-pass coordinates. */
  private Point2D start = null;

  /**
   * Checks whether the given render pass accepts the drag. When the render pass
   * accepts the drag everything is set up properly.
   * 
   * @param r The render pass to check.
   * @param position The position in canvas coordinates.
   * @param e The mouse event.
   * @return Whether the drag was accepted.
   * @see #acceptDrag(Point2D, MouseEvent)
   */
  private boolean acceptDrag(
      final Renderpass r, final Point2D position, final MouseEvent e) {
    if(!r.isVisible()) return false;
    final Rectangle2D bbox = r.getBoundingBox();
    final Point2D pos = RenderpassPainter.getPositionFromCanvas(r, position);
    if(bbox != null && !bbox.contains(pos)) return false;
    if(!r.acceptDrag(pos, e)) return false;
    start = pos;
    dragging = r;
    return true;
  }

  @Override
  public final boolean acceptDrag(final Point2D position, final MouseEvent e) {
    for(final Renderpass r : reverseList(nlFront)) {
      if(acceptDrag(r, position, e)) return true;
    }
    for(final RenderpassPosition p : reverseArray(members())) {
      if(acceptDrag(p.pass, position, e)) return true;
    }
    for(final Renderpass r : reverseList(nlBack)) {
      if(acceptDrag(r, position, e)) return true;
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
    ensureLayout();
    boolean change = false;
    Rectangle2D res = RenderpassPainter.getBoundingBox(nlFront);
    final Rectangle2D other = RenderpassPainter.getBoundingBox(nlBack);
    if(res == null) {
      res = other;
    } else if(other != null) {
      res.add(other);
    }
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
    }
    if(change) {
      invalidate();
    }
    return res == null ? new Rectangle2D.Double() : res;
  }

  @Override
  public boolean isChanging() {
    for(final Renderpass r : nlBack) {
      if(r.isChanging()) return true;
    }
    for(final Renderpass r : nlFront) {
      if(r.isChanging()) return true;
    }
    for(final RenderpassPosition rp : members) {
      if(rp.inAnimation()) return true;
    }
    return false;
  }

}
