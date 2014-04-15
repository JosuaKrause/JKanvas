package jkanvas.painter.groups;

import static jkanvas.util.ArrayUtil.*;

import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import jkanvas.Camera;
import jkanvas.KanvasContext;
import jkanvas.animation.AnimatedPosition;
import jkanvas.animation.AnimationAction;
import jkanvas.animation.AnimationList;
import jkanvas.animation.AnimationTiming;
import jkanvas.animation.Animator;
import jkanvas.animation.GenericAnimated;
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
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The type of layouted render passes.
 */
public class RenderGroup<T extends Renderpass> extends Renderpass {

  /**
   * The offset of a render pass as {@link AnimatedPosition}.
   *
   * @author Joschi <josua.krause@gmail.com>
   * @param <T> The type of the render pass.
   */
  public static final class RenderpassPosition<T extends Renderpass>
  extends GenericAnimated<Point2D> {

    /** The render pass. */
    public final T pass;

    /** The current render pass bounding box. */
    private final Rectangle2D bbox;

    /**
     * Creates a render pass position.
     *
     * @param pass The render pass.
     * @param list The animation list.
     */
    public RenderpassPosition(final T pass, final AnimationList list) {
      super(new Point2D.Double(pass.getOffsetX(), pass.getOffsetY()));
      bbox = new Rectangle2D.Double();
      pass.getBoundingBox(bbox);
      this.pass = pass;
      list.addAnimated(this);
      pass.setAnimationList(list);
    }

    @Override
    protected AnimationAction beforeAnimation(
        final AnimationTiming timing, final AnimationAction onFinish) {
      if(timing.duration <= 0) return onFinish;
      pass.setForceCache(true);
      // TODO #43 -- Java 8 simplification
      return new AnimationAction() {

        @Override
        public void animationFinished() {
          pass.setForceCache(false);
          if(onFinish != null) {
            onFinish.animationFinished();
          }
        }

      };
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
      final double oldWidth = bbox.getWidth();
      final double oldHeight = bbox.getHeight();
      RenderpassPainter.getPassBoundingBox(bbox, pass);
      return bbox.getWidth() != oldWidth || bbox.getHeight() != oldHeight;
    }

    /**
     * Getter.
     *
     * @return The bounding box of the pass. The box must not be changed. The
     *         value is refreshed by calling {@link #checkBBoxChange()}.
     */
    Rectangle2D getPassBBox() {
      return bbox;
    }

    /**
     * Getter.
     *
     * @param rect When the position is in animation the resulting frame is the
     *          destination bounding-box of the render pass. Otherwise the
     *          current bounding box is used.
     */
    public void getPredictBBox(final Rectangle2D rect) {
      pass.getBoundingBox(rect);
      final Point2D pred = getPredict();
      rect.setFrame(rect.getX() + pred.getX(),
          rect.getY() + pred.getY(), rect.getWidth(), rect.getHeight());
    }

  } // RenderpassPosition

  /** If this flag is set the layout is recomputed when the group is drawn. */
  private boolean redoLayout;

  /** The list of group members. */
  private final List<RenderpassPosition<T>> members;

  /** The list of non layouted members in front of the layouted members. */
  private final List<Renderpass> nlFront = new ArrayList<>();

  /** The list of non layouted members behind the layouted members. */
  private final List<Renderpass> nlBack = new ArrayList<>();

  /** The underlying animator. */
  private final Animator animator;

  /**
   * Creates a new render pass group.
   *
   * @param animator The underlying animator.
   */
  public RenderGroup(final Animator animator) {
    this.animator = Objects.requireNonNull(animator);
    members = new ArrayList<>();
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
   * Creates a render pass position directly without adding it to the member
   * list yet. Note that if this position will not be used it must be removed
   * with {@link #remove(RenderpassPosition)}. This method is a convenience
   * method for methods that have direct access to the member list like
   * {@link #doLayout(List)}.
   *
   * @param pass The render pass.
   * @return The converted render pass position.
   */
  protected RenderpassPosition<T> create(final T pass) {
    final RenderpassPosition<T> rp = convert(pass);
    rp.checkBBoxChange();
    addedRenderpassIntern(rp);
    return rp;
  }

  /**
   * Safely removes a render pass position directly without altering the member
   * list. This method is a convenience method for methods that have direct
   * access to the member list like {@link #doLayout(List)}.
   *
   * @param rp The render pass position.
   */
  protected void remove(final RenderpassPosition<T> rp) {
    removedRenderpassIntern(rp);
  }

  /**
   * This method is called before a render pass is added. It can be used to
   * prepare the render pass to be added.
   *
   * @param pass The render pass that will be added.
   */
  protected void beforeAdding(@SuppressWarnings("unused") final T pass) {
    // nothing to do
  }

  /**
   * Converts a render pass to a render pass position.
   *
   * @param pass The render pass.
   * @return The position.
   */
  private RenderpassPosition<T> convert(final T pass) {
    if(this == pass) throw new IllegalArgumentException("cannot add itself");
    beforeAdding(Objects.requireNonNull(pass));
    return new RenderpassPosition<>(pass, animator.getAnimationList());
  }

  /**
   * This method is called after a render pass is added.
   *
   * @param rp The render pass position.
   */
  protected void addedRenderpass(
      @SuppressWarnings("unused") final RenderpassPosition<T> rp) {
    // nothing to do
  }

  /**
   * This method is always called after a render pass is added.
   *
   * @param p The render pass position.
   */
  private void addedRenderpassIntern(final RenderpassPosition<T> p) {
    p.pass.setParent(this);
    addedRenderpass(p);
  }

  /**
   * This method is called after a render pass is removed.
   *
   * @param rp The render pass position.
   */
  protected void removedRenderpass(
      @SuppressWarnings("unused") final RenderpassPosition<T> rp) {
    // nothing to do
  }

  /**
   * This method is always called after a render pass is removed.
   *
   * @param p The render pass position.
   */
  private void removedRenderpassIntern(final RenderpassPosition<T> p) {
    removedRenderpass(p);
    p.pass.setParent(null);
  }

  /**
   * Adds a render pass.
   *
   * @param pass The render pass.
   */
  public void addRenderpass(final T pass) {
    final RenderpassPosition<T> p = convert(pass);
    members.add(p);
    addedRenderpassIntern(p);
    invalidate();
  }

  /**
   * Inserts a render pass.
   *
   * @param index The index where the render pass will be inserted.
   * @param pass The render pass.
   */
  public void addRenderpass(final int index, final T pass) {
    final RenderpassPosition<T> p = convert(pass);
    members.add(index, p);
    addedRenderpassIntern(p);
    invalidate();
  }

  /**
   * Getter.
   *
   * @param r The abstract render pass to find.
   * @return The index of the given render pass or <code>-1</code> if the pass
   *         could not be found. Equality is defined by identity.
   */
  public int indexOf(final T r) {
    for(int i = 0; i < members.size(); ++i) {
      final RenderpassPosition<T> rp = members.get(i);
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
    final RenderpassPosition<T> p = members.remove(index);
    removedRenderpassIntern(p);
    invalidate();
  }

  /** Clears all render passes. */
  public void clearRenderpasses() {
    final RenderpassPosition<T>[] rps = members();
    members.clear();
    for(final RenderpassPosition<T> p : rps) {
      removedRenderpassIntern(p);
    }
    invalidate();
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
  public T getRenderpass(final int index) {
    return members.get(index).pass;
  }

  /**
   * Sets the position of the given render pass and executes the given action
   * afterwards.
   *
   * @param pass The render pass.
   * @param pos The new position.
   * @param onFinish The (optional) action to execute afterwards.
   */
  public void setPosition(final T pass, final Point2D pos, final AnimationAction onFinish) {
    setPosition(indexOf(pass), pos, onFinish);
  }

  /**
   * Sets the position of the given render pass and executes the given action
   * afterwards.
   *
   * @param index The index of the render pass.
   * @param pos The new position.
   * @param onFinish The (optional) action to execute afterwards.
   */
  public void setPosition(final int index, final Point2D pos,
      final AnimationAction onFinish) {
    members.get(index).set(pos, onFinish);
  }

  /**
   * Transitions a render pass to the given position and executes the given
   * action afterwards.
   *
   * @param pass The render pass.
   * @param pos The new position.
   * @param timing The animation timing.
   * @param onFinish The (optional) action to execute afterwards.
   */
  public void setPosition(final T pass, final Point2D pos,
      final AnimationTiming timing, final AnimationAction onFinish) {
    setPosition(indexOf(pass), pos, timing, onFinish);
  }

  /**
   * Transitions a render pass to the given position and executes the given
   * action afterwards.
   *
   * @param index The index of the render pass.
   * @param pos The new position.
   * @param timing The animation timing.
   * @param onFinish The (optional) action to execute afterwards.
   */
  public void setPosition(final int index, final Point2D pos,
      final AnimationTiming timing, final AnimationAction onFinish) {
    members.get(index).startAnimationTo(pos, timing, onFinish);
  }

  /**
   * Setter.
   *
   * @param index The index.
   * @param pass The render pass.
   */
  public void setRenderpass(final int index, final T pass) {
    final RenderpassPosition<T> p = convert(pass);
    final RenderpassPosition<T> o = members.set(index, p);
    removedRenderpassIntern(o);
    addedRenderpassIntern(p);
    invalidate();
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
   * @param index The index where the render pass will be inserted.
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
  protected void doLayout(final List<RenderpassPosition<T>> members) {
    if(layout != null) {
      layout.doLayout(members);
    }
  }

  /** The layout of the group. */
  private RenderpassLayout<T> layout;

  /**
   * Setter.
   *
   * @param layout The layout of the group or <code>null</code> if the default
   *          layout should be used.
   */
  public void setLayout(final RenderpassLayout<T> layout) {
    this.layout = layout;
    invalidate();
  }

  /**
   * Getter.
   *
   * @return The layout of the group or <code>null</code> if the default layout
   *         should be used.
   */
  public RenderpassLayout<T> getLayout() {
    return layout;
  }

  /** Immediately computes the current layout. */
  public void forceLayout() {
    invalidate();
    doLayout(members);
    redoLayout = false;
    animator.forceNextFrame();
  }

  /** Ensures that the layout is computed. */
  private void ensureLayout() {
    if(!redoLayout) return;
    forceLayout();
  }

  /**
   * Enables to draw between two adjacent render passes.
   *
   * @param gfx The graphics context. This context must be copied implementation
   *          side and must therefore <em>not</em> be altered without creating a
   *          copy.
   * @param ctx The canvas context.
   * @param left The left (first) render pass or <code>null</code>.
   * @param right The right (second) render pass or <code>null</code>.
   */
  protected void drawBetween(@SuppressWarnings("unused") final Graphics2D gfx,
      @SuppressWarnings("unused") final KanvasContext ctx,
      @SuppressWarnings("unused") final T left, @SuppressWarnings("unused") final T right) {
    // nothing to do here
  }

  @Override
  public void draw(final Graphics2D gfx, final KanvasContext ctx) {
    ensureLayout();
    if(layout != null) {
      final Graphics2D g = (Graphics2D) gfx.create();
      final Rectangle2D bbox = new Rectangle2D.Double();
      getBoundingBox(bbox);
      layout.drawBackground(g, ctx, bbox, members);
      g.dispose();
    }
    gfx.setColor(java.awt.Color.GREEN);
    RenderpassPainter.draw(nlBack, gfx, ctx);
    final Rectangle2D view = ctx.getVisibleCanvas();
    boolean changed = false;
    T last = null;
    for(final RenderpassPosition<T> p : members) {
      final T r = p.pass;
      if(!r.isVisible()) {
        continue;
      }
      if(p.checkBBoxChange()) {
        changed = true;
      }
      final Rectangle2D bbox = p.getPassBBox();
      if(!view.intersects(bbox)) {
        continue;
      }
      final Graphics2D g = (Graphics2D) gfx.create();
      g.clip(bbox);
      final double dx = r.getOffsetX();
      final double dy = r.getOffsetY();
      g.translate(dx, dy);
      final KanvasContext c = RenderpassPainter.getContextFor(r, ctx);
      r.draw(g, c);
      g.dispose();
      drawBetween(gfx, ctx, last, r);
      last = r;
    }
    drawBetween(gfx, ctx, last, null);
    if(jkanvas.Canvas.DEBUG_BBOX) {
      final Graphics2D g = (Graphics2D) gfx.create();
      PaintUtil.setAlpha(g, 0.3);
      g.setColor(java.awt.Color.BLUE);
      for(final RenderpassPosition<T> rp : members) {
        final Renderpass r = rp.pass;
        if(!r.isVisible()) {
          continue;
        }
        if(rp.checkBBoxChange()) {
          changed = true;
        }
        final Rectangle2D bbox = rp.getPassBBox();
        if(!view.intersects(bbox)) {
          continue;
        }
        g.fill(bbox);
      }
    }
    gfx.setColor(java.awt.Color.GREEN);
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
  private RenderpassPosition<T>[] members() {
    return members.toArray(new RenderpassPosition[members.size()]);
  }

  @Override
  public boolean click(final Camera cam, final Point2D position, final MouseEvent e) {
    if(RenderpassPainter.click(nlFront, cam, position, e)) return true;
    final Rectangle2D bbox = new Rectangle2D.Double();
    for(final RenderpassPosition<T> p : reverseArray(members())) {
      final Renderpass r = p.pass;
      if(!r.isVisible()) {
        continue;
      }
      r.getBoundingBox(bbox);
      final Point2D pos = RenderpassPainter.getPositionFromCanvas(r, position);
      if(!bbox.contains(pos)) {
        continue;
      }
      if(r.click(cam, pos, e)) return true;
    }
    return RenderpassPainter.click(nlBack, cam, position, e);
  }

  @Override
  public boolean doubleClick(final Camera cam, final Point2D position, final MouseEvent e) {
    if(RenderpassPainter.doubleClick(nlFront, cam, position, e)) return true;
    final Rectangle2D bbox = new Rectangle2D.Double();
    for(final RenderpassPosition<T> p : reverseArray(members())) {
      final Renderpass r = p.pass;
      if(!r.isVisible()) {
        continue;
      }
      r.getBoundingBox(bbox);
      final Point2D pos = RenderpassPainter.getPositionFromCanvas(r, position);
      if(!bbox.contains(pos)) {
        continue;
      }
      if(r.doubleClick(cam, pos, e)) return true;
    }
    if(RenderpassPainter.doubleClick(nlBack, cam, position, e)) return true;
    if(USE_DOUBLE_CLICK_DEFAULT) return defaultDoubleClick(this, cam, e);
    return false;
  }

  @Override
  public String getTooltip(final Point2D position) {
    final String tt = RenderpassPainter.getTooltip(nlFront, position);
    if(tt != null) return tt;
    final Rectangle2D bbox = new Rectangle2D.Double();
    for(final RenderpassPosition<T> p : reverseArray(members())) {
      final Renderpass r = p.pass;
      if(!r.isVisible()) {
        continue;
      }
      r.getBoundingBox(bbox);
      final Point2D pos = RenderpassPainter.getPositionFromCanvas(r, position);
      if(!bbox.contains(pos)) {
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
    for(final RenderpassPosition<T> p : reverseArray(members())) {
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
    final Rectangle2D bbox = new Rectangle2D.Double();
    for(final RenderpassPosition<T> p : reverseArray(members())) {
      final Renderpass r = p.pass;
      if(!r.isVisible()) {
        continue;
      }
      r.getBoundingBox(bbox);
      final Point2D pos = RenderpassPainter.getPositionFromCanvas(r, position);
      if(!bbox.contains(pos)) {
        continue;
      }
      return r;
    }
    return null;
  }

  /** The render pass currently responsible for dragging. */
  private Renderpass dragging = null;

  /** The start position of the drag in the render pass coordinates. */
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
    final Rectangle2D bbox = new Rectangle2D.Double();
    r.getBoundingBox(bbox);
    final Point2D pos = RenderpassPainter.getPositionFromCanvas(r, position);
    if(!bbox.contains(pos)) return false;
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
    for(final RenderpassPosition<T> p : reverseArray(members())) {
      if(acceptDrag(p.pass, position, e)) return true;
    }
    for(final Renderpass r : reverseList(nlBack)) {
      if(acceptDrag(r, position, e)) return true;
    }
    return false;
  }

  @Override
  public final void drag(final Point2D _start, final Point2D cur,
      final double dx, final double dy) {
    if(dragging == null) return;
    // dx and dy do not change
    final Point2D pos = RenderpassPainter.getPositionFromCanvas(dragging, cur);
    dragging.drag(start, pos, dx, dy);
  }

  @Override
  public final void endDrag(final Point2D _start, final Point2D end,
      final double dx, final double dy) {
    if(dragging == null) return;
    // dx and dy do not change
    final Point2D pos = RenderpassPainter.getPositionFromCanvas(dragging, end);
    dragging.endDrag(start, pos, dx, dy);
    dragging = null;
  }

  @Override
  public void getBoundingBox(final RectangularShape bbox) {
    ensureLayout();
    boolean change = false;
    RenderpassPainter.getBoundingBox(bbox, nlFront);
    final Rectangle2D rect = new Rectangle2D.Double();
    RenderpassPainter.getBoundingBox(rect, nlBack);
    RenderpassPainter.addToRect(bbox, rect);
    if(layout != null) {
      change = layout.addBoundingBox(bbox, members);
    } else {
      for(final RenderpassPosition<T> p : members) {
        if(!p.pass.isVisible()) {
          continue;
        }
        if(p.checkBBoxChange()) {
          change = true;
        }
        RenderpassPainter.addToRect(bbox, p.getPassBBox());
      }
    }
    if(change) {
      invalidate();
    }
  }

  @Override
  public boolean isChanging() {
    for(final Renderpass r : nlBack) {
      if(r.isChanging()) return true;
    }
    for(final Renderpass r : nlFront) {
      if(r.isChanging()) return true;
    }
    for(final RenderpassPosition<T> rp : members) {
      if(rp.inAnimation()) return true;
    }
    return false;
  }

  @Override
  public void processMessage(final String[] ids, final String msg) {
    super.processMessage(ids, msg);
    RenderpassPainter.processMessage(nlBack, ids, msg);
    RenderpassPainter.processMessage(nlFront, ids, msg);
    for(final RenderpassPosition<T> p : members) {
      p.pass.processMessage(ids, msg);
    }
  }

}
