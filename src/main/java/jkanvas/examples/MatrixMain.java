package jkanvas.examples;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
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
import jkanvas.adjacency.AbstractQuadraticMatrix;
import jkanvas.adjacency.CellRealizer;
import jkanvas.adjacency.MatrixPosition;
import jkanvas.adjacency.MatrixRenderpass;
import jkanvas.adjacency.MutableQuadraticMatrix;
import jkanvas.adjacency.QuadraticMatrix;
import jkanvas.painter.RenderpassPainter;
import jkanvas.painter.StringDrawer;
import jkanvas.painter.StringDrawer.Orientation;
import jkanvas.util.PaintUtil;
import jkanvas.util.Screenshot;

/**
 * An example show-casing the painting of {@link QuadraticMatrix quadratic
 * matrices}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class MatrixMain extends MatrixRenderpass<Double> {

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

  /** The selection rectangle. */
  private Rectangle2D selection;

  @Override
  public void draw(final Graphics2D gfx, final KanvasContext ctx) {
    super.draw(gfx, ctx);
    if(selection != null) {
      final Graphics2D g = (Graphics2D) gfx.create();
      final Graphics2D g2 = (Graphics2D) g.create();
      g2.setColor(Color.PINK);
      g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, .75f));
      g2.fill(selection);
      g2.dispose();
      g.setColor(Color.BLACK);
      g.draw(selection);
      g.dispose();
    }
  }

  @Override
  public boolean acceptDrag(final Point2D p, final MouseEvent e) {
    if(!SwingUtilities.isRightMouseButton(e) && !e.isShiftDown()) return false;
    selection = new Rectangle2D.Double(p.getX(), p.getY(), 0, 0);
    return true;
  }

  @Override
  public void drag(final Point2D start, final Point2D cur, final double dx,
      final double dy) {
    final double minX = Math.min(start.getX(), cur.getX());
    final double minY = Math.min(start.getY(), cur.getY());
    final double maxX = Math.max(start.getX(), cur.getX());
    final double maxY = Math.max(start.getY(), cur.getY());
    selection = new Rectangle2D.Double(minX, minY, maxX - minX, maxY - minY);
    select();
  }

  /** Selects all cells touched by the selection rectangle. */
  private void select() {
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
  public void endDrag(final Point2D start, final Point2D cur,
      final double dx, final double dy) {
    select();
    selection = null;
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
    for(int col = 0; col < matrix.size(); ++col) {
      matrix.setName(col, "Attr" + col);
      for(int row = 0; row < matrix.size(); ++row) {
        matrix.set(row, col, Math.random());
      }
    }
    for(int i = 0; i < matrix.size(); ++i) {
      matrix.setWidth(i, 60); // 20 + Math.random() * 80);
      matrix.setHeight(i, 60); // 20 + Math.random() * 80);
    }
    final CellRealizer<Double> cellColor = new CellRealizer<Double>() {

      @Override
      public void drawCell(final Graphics2D g, final KanvasContext ctx,
          final Rectangle2D rect,
          final QuadraticMatrix<Double> matrix, final int row, final int col,
          final boolean isSelected, final boolean hasSelection) {
        final Double val = matrix.get(row, col);
        g.setColor(getColor(val, hasSelection && isSelected));
        g.fill(rect);
        g.setColor(Color.BLACK);
        g.draw(rect);
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

      private Color getColor(final double value, final boolean isSelected) {
        final double v = value - 0.5;
        final double hue = v > 0 ? 0 : 180.0 / 360.0;
        final double rv = Math.abs(v) * 2;
        return Color.getHSBColor((float) hue, (float) rv, isSelected ? 1f : .8f);
      }

    };
    final RefreshManager manager = new SimpleRefreshManager();
    final RenderpassPainter p = new RenderpassPainter();
    p.addPass(new MatrixMain(matrix, cellColor, manager));
    final Canvas c = new Canvas(p, 500, 500);
    manager.addRefreshable(c);
    c.setMargin(40);
    c.setBackground(Color.WHITE);
    final JFrame frame = new JFrame("Nodelink");
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
          Screenshot.savePNG(new File("pics"), "adjacency", c);
          System.out.println("Photo taken!");
        } catch(final IOException e) {
          e.printStackTrace();
        }
      }

    });
    frame.add(c);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    c.reset();
    frame.setVisible(true);
  }

}
