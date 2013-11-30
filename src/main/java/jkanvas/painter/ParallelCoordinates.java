package jkanvas.painter;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.util.Objects;

import jkanvas.KanvasContext;
import jkanvas.animation.AnimationTiming;
import jkanvas.animation.Animator;
import jkanvas.groups.LinearGroup;
import jkanvas.table.CachedTable;
import jkanvas.table.DataTable;

public class ParallelCoordinates {

  private final LinearGroup<AbstractRenderpass> group;

  public ParallelCoordinates(final Animator animator, final DataTable table,
      final int[] features, final double width, final double height) {
    Objects.requireNonNull(table);
    final CachedTable t;
    if(table instanceof CachedTable) {
      t = (CachedTable) table;
    } else {
      t = new CachedTable(table);
    }
    final int[] fs = features != null ? features : new int[t.cols()];
    if(features == null) {
      for(int i = 0; i < fs.length; ++i) {
        fs[i] = i;
      }
    }
    group = new LinearGroup<AbstractRenderpass>(animator, true, 0, AnimationTiming.FAST) {

      @Override
      protected void drawBetween(final Graphics2D gfx, final KanvasContext ctx,
          final AbstractRenderpass left, final AbstractRenderpass right) {
        if(left == right && left == null) return;
        final double x = left != null ? left.getOffsetX()
            + left.getBoundingBox().getWidth() : right.getOffsetX();
        final Line2D line = new Line2D.Double(x, 0, x, height);
        gfx.setColor(Color.BLACK);
        gfx.draw(line);
      }

    };
    final Rectangle2D box = new Rectangle2D.Double(0, 0, width, height);
    int lastFeature = -1;
    for(final int f : fs) {
      if(lastFeature >= 0) {
        if(f < 0) {
          group.addRenderpass(new BoxRenderpass(box));
        } else {
          group.addRenderpass(new ParallelRenderpass(t, lastFeature, f, width, height));
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
