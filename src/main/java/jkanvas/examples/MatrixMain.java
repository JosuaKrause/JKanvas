package jkanvas.examples;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

import jkanvas.Camera;
import jkanvas.Canvas;
import jkanvas.KanvasContext;
import jkanvas.RefreshManager;
import jkanvas.SimpleRefreshManager;
import jkanvas.animation.AnimationTiming;
import jkanvas.matrix.AbstractQuadraticMatrix;
import jkanvas.matrix.CellRealizer;
import jkanvas.matrix.DefaultCellRealizer;
import jkanvas.matrix.MatrixPosition;
import jkanvas.matrix.MatrixRenderpass;
import jkanvas.matrix.MutableQuadraticMatrix;
import jkanvas.matrix.QuadraticMatrix;
import jkanvas.painter.RenderpassPainter;
import jkanvas.selection.AbstractSelector;
import jkanvas.selection.RectangleSelection;
import jkanvas.selection.SelectableRenderpass;
import jkanvas.util.PaintUtil;
import jkanvas.util.StringDrawer;
import jkanvas.util.StringDrawer.Orientation;

/**
 * An example show-casing the painting of {@link QuadraticMatrix quadratic
 * matrices}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class MatrixMain extends MatrixRenderpass<QuadraticMatrix<Double>>
    implements SelectableRenderpass {

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
  public Rectangle2D getBoundingBox() {
    return PaintUtil.addPadding(super.getBoundingBox(), 60);
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
      public void drawCell(final Graphics2D g, final KanvasContext ctx,
          final Rectangle2D rect, final QuadraticMatrix<Double> matrix, final int row,
          final int col, final boolean isSelected, final boolean hasSelection) {
        super.drawCell(g, ctx, rect, matrix, row, col, isSelected, hasSelection);
        // when leftmost column draw column title
        if(col == 0) {
          final StringDrawer s = new StringDrawer(g, matrix.getName(row));
          final Point2D pos = new Point2D.Double(rect.getMinX() - 10, rect.getCenterY());
          g.setColor(Color.BLACK);
          final int h = StringDrawer.RIGHT;
          final int v = StringDrawer.CENTER_V;
          s.draw(pos, h, v);
          // g.draw(s.getBounds(pos, h, v));
          // g.setColor(Color.RED);
          // g.fill(pixel(pos));
        }
        // when topmost row draw row title
        if(row == 0) {
          final StringDrawer s = new StringDrawer(g, matrix.getName(col));
          final Point2D pos = new Point2D.Double(rect.getCenterX(), rect.getMinY() - 10);
          g.setColor(Color.BLACK);
          final int h = StringDrawer.LEFT;
          final int v = StringDrawer.BOTTOM;
          s.draw(pos, h, v, Orientation.DIAGONAL);
          // g.draw(s.getBounds(pos, h, v, Orientation.DIAGONAL));
          // g.setColor(Color.RED);
          // g.fill(pixel(pos));
        }
      }

      @Override
      protected Color getColor(final Double value, final boolean isSelected) {
        final double v = value - 0.5;
        final double hue = v > 0 ? 0 : 180.0 / 360.0;
        final double rv = Math.abs(v) * 2;
        return Color.getHSBColor((float) hue, (float) rv, isSelected ? 1f : .8f);
      }

    };
    final RefreshManager manager = new SimpleRefreshManager();
    final RenderpassPainter p = new RenderpassPainter();
    final MatrixMain matrixMain = new MatrixMain(matrix, cellColor, manager);
    p.addPass(matrixMain);
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
    // let RefreshManager refresh the Canvas
    manager.addRefreshable(c);
    // configure the Canvas
    // c.setMargin(40);
    ExampleUtil.setupCanvas("Matrix", c, p, true, false, true, false);
    c.setRestriction(p.getBoundingBox(), AnimationTiming.NO_ANIMATION);
  }

}
