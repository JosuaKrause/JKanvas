package jkanvas.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.animation.AnimationTiming;
import jkanvas.animation.Animator;
import jkanvas.groups.LinearGroup;
import jkanvas.table.DataTable;
import jkanvas.table.LineMapper;

/**
 * Parallel coordinates.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class ParallelCoordinates {

  /** The group of render passes. */
  private final LinearGroup<AbstractRenderpass> group;
  /** The size of a cell. */
  private final Rectangle2D box;
  /** The table. */
  private final DataTable table;
  /** The alpha value for the lines. */
  private final double alpha;

  /**
   * Creates parallel coordinates for the given table.
   * 
   * @param animator The animator.
   * @param table The table.
   * @param width The width of one cell.
   * @param height The height of the cells.
   * @param alpha The transparency of the lines.
   */
  public ParallelCoordinates(final Animator animator, final DataTable table,
      final double width, final double height, final double alpha) {
    Objects.requireNonNull(table);
    this.table = table.cached();
    this.alpha = alpha;
    group = new LinearGroup<AbstractRenderpass>(
        animator, true, 0, AnimationTiming.NO_ANIMATION) {

      @Override
      protected void drawBetween(final Graphics2D gfx, final KanvasContext ctx,
          final AbstractRenderpass left, final AbstractRenderpass right) {
        if(left == right && left == null) return;
        final double x;
        if(left != null) {
          final Rectangle2D leftBox = new Rectangle2D.Double();
          left.getBoundingBox(leftBox);
          x = left.getOffsetX() + leftBox.getWidth();
        } else {
          x = right.getOffsetX();
        }
        final Line2D line = new Line2D.Double(x, 0, x, height);
        gfx.setColor(Color.BLACK);
        gfx.draw(line);
      }

    };
    box = new Rectangle2D.Double(0, 0, width, height);
  }

  /**
   * Shows the given features.
   * 
   * @param features The features that should be used.
   */
  public void showFeatures(final List<Integer> features) {
    final int[] fs = new int[features.size()];
    for(int i = 0; i < fs.length; ++i) {
      fs[i] = features.get(i);
    }
    showFeatures(fs);
  }

  /**
   * Shows the given features.
   * 
   * @param features The features that should be used or <code>null</code> if
   *          all features should be used.
   */
  public void showFeatures(final int[] features) {
    group.clearRenderpasses();
    final int[] fs = features != null ? features : new int[table.cols()];
    if(features == null) {
      for(int i = 0; i < fs.length; ++i) {
        fs[i] = i;
      }
    }
    int lastFeature = -1;
    for(final int f : fs) {
      if(lastFeature >= 0) {
        if(f < 0) {
          group.addRenderpass(new BoxRenderpass(box));
        } else {
          final LineMapper lm = new LineMapper(table, lastFeature, f,
              box.getWidth(), box.getHeight());
          final ParallelRenderpass pr = new ParallelRenderpass(lm, alpha);
          pr.getList().setDefaultColor(new Color(0x9EBCDA));
          group.addRenderpass(pr);
        }
      }
      lastFeature = f;
    }
  }

  /**
   * Getter.
   * 
   * @return The actual render pass.
   */
  public AbstractRenderpass getRenderpass() {
    return group;
  }

}
