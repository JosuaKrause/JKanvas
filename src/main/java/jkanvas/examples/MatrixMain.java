package jkanvas.examples;

import java.awt.Color;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

import jkanvas.Camera;
import jkanvas.Canvas;
import jkanvas.RefreshManager;
import jkanvas.animation.AnimatedPainter;
import jkanvas.animation.AnimationTiming;
import jkanvas.matrix.AbstractQuadraticMatrix;
import jkanvas.matrix.CellRealizer;
import jkanvas.matrix.DefaultCellRealizer;
import jkanvas.matrix.MatrixPosition;
import jkanvas.matrix.MatrixRenderpass;
import jkanvas.matrix.MutableQuadraticMatrix;
import jkanvas.matrix.QuadraticMatrix;
import jkanvas.painter.Renderpass;
import jkanvas.painter.TitleRenderpass;
import jkanvas.painter.TitleRenderpass.Position;
import jkanvas.selection.AbstractSelector;
import jkanvas.selection.RectangleSelection;
import jkanvas.selection.Selectable;
import jkanvas.util.StringDrawer.Orientation;

/**
 * An example show-casing the painting of {@link QuadraticMatrix quadratic
 * matrices}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
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
    final QuadraticMatrix<Double> matrix = getMatrix();
    final MatrixPosition pos = pick(p);
    if(pos == null) return "";
    return "row: " + matrix.getName(pos.row)
        + " col: " + matrix.getName(pos.col)
        + " value: " + matrix.get(pos.row, pos.col);
  }

  @Override
  public void select(final Shape selection, final boolean preview) {
    final QuadraticMatrix<Double> matrix = getMatrix();
    for(int row = 0; row < matrix.size(); ++row) {
      for(int col = 0; col < matrix.size(); ++col) {
        final Rectangle2D bbox = matrix.getBoundingBox(row, col);
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
   */
  public static void main(final String[] args) {
    final MutableQuadraticMatrix<Double> matrix = new AbstractQuadraticMatrix<Double>(9) {

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
    final CellRealizer<QuadraticMatrix<Double>> cellColor = new DefaultCellRealizer<Double, QuadraticMatrix<Double>>() {

      @Override
      protected Color getColor(final Double value, final boolean isSelected) {
        final double v = value - 0.5;
        final double hue = v > 0 ? 0 : 180.0 / 360.0;
        final double rv = Math.abs(v) * 2;
        return Color.getHSBColor((float) hue, (float) rv, isSelected ? 1f : .8f);
      }

    };
    // FIXME animated painter because of initial reset -- fix in ExampleUtil #28
    final AnimatedPainter p = new AnimatedPainter();
    final MatrixMain matrixMain = new MatrixMain(matrix, cellColor, p);
    final String[] odds = matrixMain.getNames();
    final String[] evens = matrixMain.getNames();
    for(int i = 0; i < odds.length; ++i) {
      if(i % 2 == 0) {
        odds[i] = "";
      } else {
        evens[i] = "";
      }
    }
    final TitleRenderpass<MatrixMain> top =
        new TitleRenderpass<>(matrixMain, "", 40, 5);
    top.setOrientation(Orientation.DIAGONAL);
    top.setTitles(evens);
    final TitleRenderpass<MatrixMain> left = new TitleRenderpass<>(top, "", 40, 5);
    left.setPosition(Position.LEFT);
    left.setTitles(evens);
    final TitleRenderpass<MatrixMain> right = new TitleRenderpass<>(left, "", 40, 5);
    right.setPosition(Position.RIGHT);
    right.setTitles(odds);
    final TitleRenderpass<MatrixMain> bottom = new TitleRenderpass<>(right, "", 40, 5);
    bottom.setPosition(Position.BELOW);
    bottom.setOrientation(Orientation.VERTICAL);
    bottom.setTitles(odds);
    p.addPass(bottom);
    final Canvas c = new Canvas(p, true, 500, 500);
    // add arbitrary shape selection
    final AbstractSelector sel = new RectangleSelection(c,
        // final AbstractSelector sel = new LassoSelection(c,
        new Color(5, 113, 176, 200)) {

      @Override
      public boolean acceptDragHUD(final Point2D p, final MouseEvent e) {
        return SwingUtilities.isLeftMouseButton(e) && e.isShiftDown();
      }

    };
    sel.addSelectable(matrixMain);
    p.addHUDPass(sel);
    // configure the Canvas
    // c.setMargin(40);
    ExampleUtil.setupCanvas("Matrix", c, p, true, false, true, false);
    final Rectangle2D box = new Rectangle2D.Double();
    p.getBoundingBox(box);
    c.setRestriction(box, AnimationTiming.NO_ANIMATION);
  }

}
