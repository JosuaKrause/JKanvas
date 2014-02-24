package jkanvas.nodelink.layout;

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.util.Objects;

import jkanvas.Canvas;
import jkanvas.animation.AnimatedPosition;
import jkanvas.animation.AnimationAction;
import jkanvas.animation.AnimationTiming;
import jkanvas.nodelink.NodeLinkView;

/**
 * An abstract class for laying out node-link diagrams.
 * 
 * @author Joschi <josua.krause@gmail.com>
 * @param <T> The position type.
 */
public abstract class AbstractLayouter<T extends AnimatedPosition> {

  /** The node-link view. */
  private NodeLinkView<T> view;
  /** The registered canvas. */
  private Canvas canvas;
  /** The timing of the animation. */
  private AnimationTiming timing;
  /**
   * The rectangle in which the layout should be created. When <code>null</code>
   * the current view is used.
   */
  private Rectangle2D rect;

  /** Creates an abstract layouter. */
  public AbstractLayouter() {
    timing = AnimationTiming.SMOOTH;
  }

  /**
   * Setter.
   * 
   * @param timing The timing.
   */
  public void setTiming(final AnimationTiming timing) {
    this.timing = Objects.requireNonNull(timing);
  }

  /**
   * Getter.
   * 
   * @return The timing.
   */
  public AnimationTiming getTiming() {
    return timing;
  }

  /**
   * Register this layouter to calculate the layout of the given view.
   * 
   * @param canvas The canvas.
   * @param view The view.
   */
  public void register(final Canvas canvas, final NodeLinkView<T> view) {
    if(this.view != null) throw new IllegalStateException(
        "view must be deregistered first: " + this.view + " new: " + view);
    this.canvas = Objects.requireNonNull(canvas);
    this.view = Objects.requireNonNull(view);
  }

  /** Deregisters a layouter. */
  public void deregister() {
    if(view == null) throw new IllegalStateException("no view to deregister");
    canvas = null;
    view = null;
  }

  /** The precision of coordinate equivalence. */
  public static double PRECISION = 1e-12;

  /**
   * Sets the position of the given node.
   * 
   * @param node The node.
   * @param pos The new position.
   * @return If the position was altered.
   */
  protected boolean setPosition(final T node, final Point2D pos) {
    final Point2D predict = node.getPredict();
    if(Math.abs(predict.getX() - pos.getX()) < PRECISION
        && Math.abs(predict.getY() - pos.getY()) < PRECISION) {
      if(node.inAnimation()) {
        node.startAnimationTo(pos, timing);
      } else {
        node.setPosition(pos);
      }
      return false;
    }
    node.startAnimationTo(pos, timing);
    return true;
  }

  /**
   * Calculates the layout for the given view.
   * 
   * @param view The view.
   * @return If the position was altered.
   */
  protected abstract boolean doLayout(NodeLinkView<T> view);

  /**
   * Getter.
   * 
   * @return Whether layout calculation should be iterative until no nodes are
   *         moved anymore.
   */
  protected boolean iterate() {
    return false;
  }

  /**
   * Starts the layout.
   * 
   * @param deregisterOnEnd Whether to unregister the layouter after completion.
   */
  public void layout(final boolean deregisterOnEnd) {
    startLayout(deregisterOnEnd);
  }

  /**
   * Computes the layout.
   * 
   * @param deregisterOnEnd Whether to unregister the layouter after completion.
   */
  protected void startLayout(final boolean deregisterOnEnd) {
    // check whether we are still registered
    if(view == null) return;
    final boolean chg = doLayout(view);
    if(!chg || !iterate()) {
      if(deregisterOnEnd) {
        deregister();
      }
      return;
    }
    // TODO #43 -- Java 8 simplification
    canvas.scheduleAction(new AnimationAction() {

      @Override
      public void animationFinished() {
        startLayout(deregisterOnEnd);
      }

    }, timing);
  }

  /**
   * Setter.
   * 
   * @param rect The rectangle to bound the layout. If this value is
   *          <code>null</code> the current viewport is used.
   */
  public void setRectangle(final Rectangle2D rect) {
    this.rect = rect;
  }

  /**
   * Getter.
   * 
   * @param bbox The rectangle in which the bounding box of the layout will be
   *          stored.
   */
  public void getBoundingBox(final RectangularShape bbox) {
    bbox.setFrame(rect == null ? canvas.getVisibleCanvas() : rect);
  }

}
