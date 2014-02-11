package jkanvas.examples;

import java.awt.Color;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import jkanvas.Camera;
import jkanvas.Canvas;
import jkanvas.RefreshManager;
import jkanvas.io.json.JSONManager;
import jkanvas.io.json.JSONSetup;
import jkanvas.matrix.AbstractMutableQuadraticMatrix;
import jkanvas.matrix.CellRealizer;
import jkanvas.matrix.DefaultCellRealizer;
import jkanvas.matrix.Matrix;
import jkanvas.matrix.MatrixPosition;
import jkanvas.matrix.MatrixRenderpass;
import jkanvas.matrix.QuadraticMatrix;
import jkanvas.painter.Renderpass;
import jkanvas.painter.RenderpassPainter;
import jkanvas.selection.AbstractSelector;
import jkanvas.selection.RectangleSelection;
import jkanvas.selection.Selectable;
import jkanvas.util.Resource;

/**
 * An example show-casing the painting of {@link QuadraticMatrix quadratic
 * matrices}.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public class MatrixMain extends MatrixRenderpass<QuadraticMatrix<Double>>
    implements Selectable {

  /**
   * Creates a matrix painter.
   * 
   * @param matrix The matrix.
   * @param cellDrawer The cell realizer.
   * @param manager The update manager.
   */
  public MatrixMain(final QuadraticMatrix<Double> matrix,
      final CellRealizer<QuadraticMatrix<Double>> cellDrawer,
      final RefreshManager manager) {
    super(matrix, cellDrawer, manager);
  }

  /** The selected cells. */
  private final Set<MatrixPosition> selected = new HashSet<>();

  @Override
  protected boolean isSelected(final int row, final int col) {
    for(final MatrixPosition pos : selected) {
      if(row == pos.row || col == pos.col) return true;
    }
    return false;
  }

  @Override
  public boolean click(final Camera cam, final Point2D p, final MouseEvent e) {
    if(!SwingUtilities.isRightMouseButton(e) || e.isShiftDown()) return false;
    final MatrixPosition pos = pick(p);
    if(pos == null) return false;
    if(selected.contains(pos)) {
      selected.remove(pos);
    } else {
      selected.add(pos);
    }
    return true;
  }

  @Override
  public String getTooltip(final Point2D p) {
    final Matrix<Double> matrix = getMatrix();
    final MatrixPosition pos = pick(p);
    if(pos == null) return "";
    return "row: " + matrix.getRowName(pos.row)
        + " col: " + matrix.getColumnName(pos.col)
        + " value: " + matrix.get(pos);
  }

  @Override
  public void select(final Shape selection, final boolean preview) {
    final Matrix<Double> matrix = getMatrix();
    for(int row = 0; row < matrix.rows(); ++row) {
      for(int col = 0; col < matrix.cols(); ++col) {
        final Rectangle2D bbox = new Rectangle2D.Double();
        matrix.getBoundingBox(bbox, row, col);
        final MatrixPosition m = new MatrixPosition(row, col);
        if(selection.intersects(bbox)/* selection.contains(bbox) */) {
          selected.add(m);
        } else {
          selected.remove(m);
        }
      }
    }
  }

  @Override
  public Renderpass getRenderpass() {
    return this;
  }

  /**
   * Starts the example application.
   * 
   * @param args No arguments.
   * @throws IOException I/O Exception.
   */
  public static void main(final String[] args) throws IOException {
    // TODO #43 -- Java 8 simplification
    final AbstractMutableQuadraticMatrix<Double> matrix = new AbstractMutableQuadraticMatrix<Double>(
        9) {

      @Override
      protected Double[][] createMatrix(final int size) {
        return new Double[size][size];
      }

    };
    // fill the matrix with random values
    for(int col = 0; col < matrix.size(); ++col) {
      for(int row = 0; row < matrix.size(); ++row) {
        matrix.set(row, col, Math.random());
      }
    }
    // set names, widths, and heights of rows / columns
    for(int i = 0; i < matrix.size(); ++i) {
      matrix.setName(i, "Attr" + i);
      matrix.setWidth(i, 60); // 20 + Math.random() * 80);
      matrix.setHeight(i, 60); // 20 + Math.random() * 80);
    }
    final String[] odds = matrix.getNames();
    final String[] evens = matrix.getNames();
    for(int i = 0; i < odds.length; ++i) {
      if(i % 2 == 0) {
        odds[i] = "";
      } else {
        evens[i] = "";
      }
    }
    // TODO #43 -- Java 8 simplification
    final CellRealizer<QuadraticMatrix<Double>> cellColor = new DefaultCellRealizer<Double, QuadraticMatrix<Double>>() {

      @Override
      protected Color getColor(final Double value, final boolean isSelected) {
        final double v = value - 0.5;
        final double hue = v > 0 ? 0 : 180.0 / 360.0;
        final double rv = Math.abs(v) * 2;
        return Color.getHSBColor((float) hue, (float) rv, isSelected ? 1f : .8f);
      }

    };
    final JSONManager mng = new JSONManager();
    mng.addRawId("matrix", matrix);
    mng.addRawId("cell", cellColor);
    mng.addRawId("odds", odds);
    mng.addRawId("evens", evens);
    final JFrame frame = new JFrame("Matrix");
    JSONSetup.setupCanvas(frame, mng, Resource.getFor("matrix.json"), false, true);
    // add arbitrary shape selection
    final Canvas c = mng.getForId("canvas", Canvas.class);
    // TODO #43 -- Java 8 simplification
    final AbstractSelector sel = new RectangleSelection(c,
        // final AbstractSelector sel = new LassoSelection(c,
        new Color(5, 113, 176, 200)) {

      @Override
      public boolean acceptDragHUD(final Point2D p, final MouseEvent e) {
        return SwingUtilities.isLeftMouseButton(e) && e.isShiftDown();
      }

    };
    sel.addSelectable(mng.getForId("main", Selectable.class));
    final RenderpassPainter p = mng.getForId("painter", RenderpassPainter.class);
    p.addHUDPass(sel);
    frame.setVisible(true);
  }

}
