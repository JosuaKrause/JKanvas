package jkanvas.painter;

import java.util.BitSet;
import java.util.Objects;

import jkanvas.animation.AnimationTiming;
import jkanvas.animation.Animator;
import jkanvas.animation.CircleList;
import jkanvas.painter.groups.LinearGroup;
import jkanvas.painter.pod.BorderRenderpass;
import jkanvas.table.DataTable;
import jkanvas.table.PointListFactory;
import jkanvas.table.PointMapper;

/**
 * A scatter plot matrix.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class ScatterplotMatrix {

  /** The group of render passes. */
  private final LinearGroup<Renderpass> group;

  /**
   * Creates a scatter plot matrix for the given table. Note that the data is
   * not connected to scatter plot after creating.
   * 
   * @param animator The animator.
   * @param table The table.
   * @param size The size of the scatter plots.
   * @param space The space between scatter plots.
   * @param pointSize The size of the points.
   * @param factory The point list factory or <code>null</code> if
   *          {@link CircleList circle lists} should be used.
   */
  public ScatterplotMatrix(final Animator animator, final DataTable table,
      final double size, final double space, final double pointSize,
      final PointListFactory factory) {
    Objects.requireNonNull(table);
    final DataTable t = table.cached();
    group = new LinearGroup<>(
        animator, true, space, AnimationTiming.FAST);
    group.setBreakPoint((size + space) * t.cols() - space);
    final ScatterplotRenderpass[] sr = new ScatterplotRenderpass[
        indexOfRow(t, t.cols())];
    for(int row = 0; row < t.cols(); ++row) {
      final int off = indexOfRow(t, row) - row;
      for(int col = row; col < t.cols(); ++col) {
        final PointMapper pm = new PointMapper(
            t, row, col, size, pointSize, pointSize * 2);
        if(factory != null) {
          pm.setPointListFactory(factory);
        }
        sr[off + col] = new ScatterplotRenderpass(pm);
      }
    }
    final BitSet already = new BitSet();
    for(int row = 0; row < t.cols(); ++row) {
      for(int col = 0; col < t.cols(); ++col) {
        final int index = indexOfRow(t, Math.min(row, col))
            - Math.min(row, col) + Math.max(row, col);
        final Renderpass rp;
        if(already.get(index)) {
          rp = new GhostRenderpass<>(sr[index]);
        } else {
          already.set(index);
          rp = sr[index];
        }
        group.addRenderpass(new BorderRenderpass<>(rp));
      }
    }
  }

  /**
   * Compute the index of a row in the flat array.
   * 
   * @param table The table.
   * @param row The row.
   * @return The index.
   */
  private static int indexOfRow(final DataTable table, final int row) {
    final int size = table.cols();
    return (2 * size - row + 1) * row / 2;
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
