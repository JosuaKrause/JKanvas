package jkanvas.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

import jkanvas.KanvasContext;
import jkanvas.animation.AnimationTiming;
import jkanvas.animation.Animator;
import jkanvas.painter.groups.LinearGroup;
import jkanvas.table.DataTable;
import jkanvas.table.Feature;
import jkanvas.table.LineMapper;

/**
 * Parallel coordinates.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class ParallelCoordinates {

  /** The group of render passes. */
  private final LinearGroup<Renderpass> group;
  /** The size of a cell. */
  private final Rectangle2D box;
  /** The alpha value for the lines. */
  private final double alpha;

  /**
   * Creates parallel coordinates for the given table.
   * 
   * @param animator The animator.
   * @param width The width of one cell.
   * @param height The height of the cells.
   * @param alpha The transparency of the lines.
   */
  public ParallelCoordinates(final Animator animator,
      final double width, final double height, final double alpha) {
    this.alpha = alpha;
    // TODO #43 -- Java 8 simplification
    group = new LinearGroup<Renderpass>(
        animator, true, 0, AnimationTiming.NO_ANIMATION) {

      @Override
      protected void drawBetween(final Graphics2D gfx, final KanvasContext ctx,
          final Renderpass left, final Renderpass right) {
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
  public void showFeatures(final List<Feature> features) {
    DataTable table = null;
    final int[] fs = new int[features.size()];
    for(int i = 0; i < fs.length; ++i) {
      final Feature f = features.get(i);
      if(table == null) {
        table = f.getTable();
      } else if(table != f.getTable()) throw new IllegalArgumentException(
          "features must be from same table: " + table + " != " + f.getTable());
      fs[i] = f.getColumn();
    }
    showFeatures(table, fs);
  }

  /**
   * Shows the given features.
   * 
   * @param table The data table.
   * @param features The features that should be used or <code>null</code> if
   *          all features should be used.
   */
  public void showFeatures(final DataTable table, final int[] features) {
    group.clearRenderpasses();
    if(table == null) return;
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
          final LineMapper lm = new LineMapper(table, lastFeature,
              f, box.getWidth(), box.getHeight(), alpha);
          final ParallelRenderpass pr = new ParallelRenderpass(lm);
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
  public Renderpass getRenderpass() {
    return group;
  }

}
