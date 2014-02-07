package jkanvas.examples;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

import jkanvas.Camera;
import jkanvas.Canvas;
import jkanvas.animation.AnimatedPainter;
import jkanvas.animation.AnimationTiming;
import jkanvas.matrix.AnimatedMatrix;
import jkanvas.matrix.CellRealizer;
import jkanvas.matrix.DefaultCellRealizer;
import jkanvas.matrix.Matrix;
import jkanvas.matrix.MatrixPosition;
import jkanvas.matrix.MatrixRenderpass;

public class AnimatedMatrixMain {

  private AnimatedMatrixMain() {
    throw new AssertionError();
  }

  /**
   * Starts the example application.
   * 
   * @param args No arguments.
   * @throws IOException I/O Exception.
   */
  public static void main(final String[] args) throws IOException {
    final AnimatedMatrix<Double> am = new AnimatedMatrix<>(0.0, 10.0, 10.0, "r0", "c0");
    final CellRealizer<Matrix<Double>> cd = new DefaultCellRealizer<Double, Matrix<Double>>() {

      @Override
      protected Color getColor(final Double value, final boolean isSelected) {
        return Color.getHSBColor((float) ((double) value), 0.8f, 0.8f);
      }

    };
    final AnimatedPainter ap = new AnimatedPainter();
    final MatrixRenderpass<Matrix<Double>> mr = new MatrixRenderpass<Matrix<Double>>(
        am, cd, ap) {

      @Override
      public boolean click(final Camera cam, final Point2D pos, final MouseEvent e) {
        if(!SwingUtilities.isRightMouseButton(e)) return false;
        final MatrixPosition p = pick(pos);
        final List<List<Double>> els = new ArrayList<>();
        els.add(new ArrayList<Double>());
        els.add(new ArrayList<Double>());
        final List<String> names = Arrays.asList("0", "1");
        final List<Double> sizes = Arrays.asList(10.0, 10.0);
        final AnimationTiming timing = AnimationTiming.SMOOTH;
        if(e.isShiftDown()) { // columns
          final int c = p.col;
          for(int r = 0; r < am.rows(); ++r) {
            final double v = am.get(r, c);
            els.get(0).add(shift(v, -1));
            els.get(1).add(shift(v, 1));
          }
          am.replaceColumns(c, c + 1, els, names, sizes, timing);
        } else {// rows
          final int r = p.row;
          for(int c = 0; c < am.cols(); ++c) {
            final double v = am.get(r, c);
            els.get(0).add(shift(v, -1));
            els.get(1).add(shift(v, 1));
          }
          am.replaceColumns(r, r + 1, els, names, sizes, timing);
        }
        return true;
      }

      private double shift(final double v, final double dir) {
        final double shift = v + dir * 10.0 / 360.0;
        final double integer = Math.floor(Math.abs(shift));
        return shift < 0 ? shift + integer + 1 : shift - integer;
      }

    };
    ap.addPass(mr);
    final Canvas c = new Canvas(ap, 500, 500);
    ExampleUtil.setupCanvas("animated-matrix", c, ap, true, true, true, false);
  }
}
