package jkanvas.examples;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.SwingUtilities;

import jkanvas.Camera;
import jkanvas.Canvas;
import jkanvas.CanvasSetup;
import jkanvas.animation.AnimatedPainter;
import jkanvas.animation.AnimationTiming;
import jkanvas.matrix.AnimatedMatrix;
import jkanvas.matrix.CellRealizer;
import jkanvas.matrix.DefaultCellRealizer;
import jkanvas.matrix.Matrix;
import jkanvas.matrix.MatrixPosition;
import jkanvas.matrix.MatrixRenderpass;
import jkanvas.painter.pod.Renderpod;

/**
 * An example application to play around with animated matrices.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class AnimatedMatrixMain {

  /** No constructor. */
  private AnimatedMatrixMain() {
    throw new AssertionError();
  }

  /**
   * Starts the example application.
   * 
   * @param args No arguments.
   */
  public static void main(final String[] args) {
    final AnimatedMatrix<Double> am = new AnimatedMatrix<>(0.0, 10.0, 10.0, "0", "0");
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
        final List<Double> sizes = Arrays.asList(10.0, 10.0);
        final AnimationTiming timing = AnimationTiming.SMOOTH;
        if(!e.isAltDown())
        if(e.isShiftDown()) { // columns
          final int c = p.col;
          final List<String> names = Arrays.asList("" + c, "" + (c + 1));
          for(int r = 0; r < am.rows(); ++r) {
            final double v = am.get(r, c);
            els.get(0).add(shift(v, -1));
            els.get(1).add(shift(v, 1));
          }
          am.replaceColumns(c, c + 1, els, names, sizes, timing);
        } else { // rows
          final int r = p.row;
          final List<String> names = Arrays.asList("" + r, "" + (r + 1));
          for(int c = 0; c < am.cols(); ++c) {
            final double v = am.get(r, c);
            els.get(0).add(shift(v, -1));
            els.get(1).add(shift(v, 1));
          }
          am.replaceRows(r, r + 1, els, names, sizes, timing);
        }
        else if(e.isShiftDown()) {
          if(am.cols() > 1) {
            am.removeColumns(p.col, p.col + 1, timing);
          }
        } else if(am.rows() > 1) {
          am.removeRows(p.row, p.row + 1, timing);
        }
        return true;
      }

      private double shift(final double v, final double dir) {
        final double shift = v + dir * 10.0 / 360.0;
        final double integer = Math.floor(Math.abs(shift));
        return shift < 0 ? shift + integer + 1 : shift - integer;
      }

    };
    final Renderpod<MatrixRenderpass<Matrix<Double>>> pod =
        MatrixRenderpass.createTitledMatrixRenderpass(mr, 10, 10);
    ap.addPass(pod);
    final Canvas c = new Canvas(ap, 500, 500);
    CanvasSetup.setupCanvas("animated-matrix", c, ap, true, true, true, false);
  }

}
