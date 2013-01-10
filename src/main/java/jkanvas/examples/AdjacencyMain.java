package jkanvas.examples;

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
import jkanvas.adjacency.AbstractAdjacencyMatrix;
import jkanvas.adjacency.AdjacencyMatrix;
import jkanvas.adjacency.CellRealizer;
import jkanvas.adjacency.MatrixPainter;
import jkanvas.adjacency.MatrixPosition;
import jkanvas.painter.StringDrawer;
import jkanvas.painter.StringDrawer.Orientation;
import jkanvas.util.Screenshot;

/**
 * An example showcasing the painting of {@link AdjacencyMatrix adjacency
 * matrices}.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public class AdjacencyMain {

  /**
   * Starts the example application.
   * 
   * @param args No arguments.
   */
  public static void main(final String[] args) {
    final AdjacencyMatrix<Double> matrix = new AbstractAdjacencyMatrix<Double>(9) {

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
          final AdjacencyMatrix<Double> matrix, final int row, final int col,
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
    final MatrixPainter<Double> p = new MatrixPainter<Double>(matrix, cellColor) {

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
        if(!SwingUtilities.isRightMouseButton(e)) return false;
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
        final AdjacencyMatrix<Double> matrix = getMatrix();
        final MatrixPosition pos = pick(p);
        if(pos == null) return "";
        return "row: " + matrix.getName(pos.row)
            + " col: " + matrix.getName(pos.col)
            + " value: " + matrix.get(pos.row, pos.col);
      }

    };
    final Canvas c = new Canvas(p, 500, 500);
    matrix.addRefreshable(c);
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
