package jkanvas.examples;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import jkanvas.Canvas;
import jkanvas.KanvasContext;
import jkanvas.RefreshManager;
import jkanvas.SimpleRefreshManager;
import jkanvas.matrix.AbstractQuadraticMatrix;
import jkanvas.matrix.CellRealizer;
import jkanvas.matrix.MatrixPosition;
import jkanvas.matrix.MatrixRenderpass;
import jkanvas.matrix.MutableQuadraticMatrix;
import jkanvas.matrix.QuadraticMatrix;
import jkanvas.painter.RenderpassPainter;
import jkanvas.painter.StringDrawer;
import jkanvas.painter.StringDrawer.Orientation;
import jkanvas.selection.AbstractSelector;
import jkanvas.selection.RectangleSelection;
import jkanvas.selection.SelectableRenderpass;
import jkanvas.util.PaintUtil;
import jkanvas.util.Screenshot;

/**
 * An example show-casing the painting of {@link QuadraticMatrix quadratic
 * matrices}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class MatrixMain extends MatrixRenderpass<Double> implements SelectableRenderpass {

  /**
   * Creates a matrix painter.
   * 
   * @param matrix The matrix.
   * @param cellDrawer The cell realizer.
   * @param manager The update manager.
   */
  public MatrixMain(final QuadraticMatrix<Double> matrix,
      final CellRealizer<Double> cellDrawer, final RefreshManager manager) {
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
  public boolean click(final Point2D p, final MouseEvent e) {
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
    final MutableQuadraticMatrix<Double> matrix =
        new AbstractQuadraticMatrix<Double>(9) {

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
    final CellRealizer<Double> cellColor = new CellRealizer<Double>() {

      @Override
      public void drawCell(final Graphics2D g, final KanvasContext ctx,
          final Rectangle2D rect, final QuadraticMatrix<Double> matrix, final int row,
          final int col, final boolean isSelected, final boolean hasSelection) {
        final Double val = matrix.get(row, col);
        g.setColor(getColor(val, hasSelection && isSelected));
        g.fill(rect);
        g.setColor(Color.BLACK);
        g.draw(rect);
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

      /**
       * Determines the color for the given value.
       * 
       * @param value The value.
       * @param isSelected Whether the cell is selected.
       * @return The color of the cell.
       */
      private Color getColor(final double value, final boolean isSelected) {
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
    final Canvas c = new Canvas(p, 500, 500);
    // add arbitrary shape selection
    final AbstractSelector sel = new RectangleSelection(c,
        // final AbstractSelector sel = new LassoSelection(c,
        new Color(5, 113, 176, 200)) {

      @Override
      public boolean acceptDragHUD(final Point2D p, final MouseEvent e) {
        return SwingUtilities.isRightMouseButton(e) && e.isShiftDown();
      }

    };
    sel.addSelectable(matrixMain);
    p.addHUDPass(sel);
    // let RefreshManager refresh the Canvas
    manager.addRefreshable(c);
    // configure the Canvas
    // c.setMargin(40);
    c.setBackground(Color.WHITE);
    final JFrame frame = new JFrame("Matrix");
    // add actions to the Canvas
    c.addAction(KeyEvent.VK_Q, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        frame.dispose();
      }

    });
    c.addAction(KeyEvent.VK_R, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        c.reset();
      }

    });
    c.addAction(KeyEvent.VK_P, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent ae) {
        try {
          Screenshot.savePNG(new File("pics"), "matrix", c);
          System.out.println("Photo taken!");
        } catch(final IOException e) {
          e.printStackTrace();
        }
      }

    });
    // pack and show window
    frame.add(c);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    c.reset();
    frame.setVisible(true);
  }

}
