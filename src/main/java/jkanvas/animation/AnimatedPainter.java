package jkanvas.animation;

import java.util.ArrayList;
import java.util.List;

import jkanvas.Refreshable;
import jkanvas.painter.RenderpassPainter;

/**
 * An animated painter.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class AnimatedPainter extends RenderpassPainter implements Animator {

  /** The internal animator. */
  private AbstractAnimator animator;

  /** Creates an animated painter. */
  public AnimatedPainter() {
    animator = new AbstractAnimator() {

      @Override
      protected boolean step() {
        final long currentTime = System.currentTimeMillis();
        return doStep(currentTime);
      }

    };
  }

  /** All registered layouters. */
  private final List<AnimatedLayouter> layouters = new ArrayList<>();

  /**
   * Adds an animatable layouter.
   * 
   * @param layouter The layouter.
   */
  public void addLayouter(final AnimatedLayouter layouter) {
    if(layouters.contains(layouter)) throw new IllegalArgumentException(
        "layouter already added: " + layouter);
    layouters.add(layouter);
  }

  /**
   * Removes an animatable layouter.
   * 
   * @param layouter The layouter.
   */
  public void removeLayouter(final AnimatedLayouter layouter) {
    layouters.remove(layouter);
  }

  /**
   * Computes one step for all layouters.
   * 
   * @param currentTime The current time in milliseconds.
   * @return Whether a redraw is needed.
   */
  protected boolean doStep(final long currentTime) {
    boolean needsRedraw = false;
    for(final AnimatedLayouter l : layouters) {
      for(final AnimatedPosition node : l.getPositions()) {
        node.animate(currentTime);
        needsRedraw = node.hasChanged() || needsRedraw;
      }
    }
    return needsRedraw;
  }

  /**
   * Getter.
   * 
   * @return The animation lock.
   */
  public Object getAnimationLock() {
    return animator.getAnimationLock();
  }

  @Override
  public void addRefreshable(final Refreshable r) {
    animator.addRefreshable(r);
  }

  @Override
  public void removeRefreshable(final Refreshable r) {
    animator.removeRefreshable(r);
  }

  @Override
  public void forceNextFrame() {
    animator.forceNextFrame();
  }

  @Override
  public void quickRefresh() {
    animator.quickRefresh();
  }

  @Override
  public void startBulkOperation() {
    animator.startBulkOperation();
  }

  @Override
  public void endBulkOperation() {
    animator.endBulkOperation();
  }

  @Override
  public Refreshable[] getRefreshables() {
    return animator.getRefreshables();
  }

  @Override
  public void refreshAll() {
    animator.refreshAll();
  }

  /** Disposes this painter and stops the animator. */
  @Override
  public void dispose() {
    if(animator.isDisposed()) return;
    animator.dispose();
  }

}
