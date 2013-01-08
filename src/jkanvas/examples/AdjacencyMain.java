package jkanvas.examples;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import jkanvas.Canvas;
import jkanvas.KanvasContext;
import jkanvas.painter.Renderpass;
import jkanvas.painter.RenderpassPainter;

public class AdjacencyMain {

  private static class Position {

    public final int row;

    public final int col;

    public Position(final int row, final int col) {
      this.row = row;
      this.col = col;
    }

  }

  private static class AdjacencyMatrix {

    private final String[] names;

    private final double[][] matrix;

    public AdjacencyMatrix(final int size) {
      names = new String[size];
      matrix = new double[size][];
      for(int i = 0; i < size; ++i) {
        matrix[i] = new double[size];
      }
    }

    public String getName(final int row) {
      return names[row] == null ? "" : names[row];
    }

    public void setName(final int row, final String name) {
      names[row] = name;
    }

    public Color getColorFor(final int row, final int col) {
      final double v = get(row, col) - 0.5;
      final double hue = v > 0 ? 0 : 180;
      final double rv = Math.abs(v) * 2;
      return Color.getHSBColor((float) hue, (float) rv, 0.8f);
    }

    public double get(final int row, final int col) {
      return matrix[row][col];
    }

    public void set(final int row, final int col, final double value) {
      matrix[row][col] = value;
    }

    public int size() {
      return names.length;
    }

  }

  private static class MatrixRenderpass extends RenderpassPainter implements Renderpass {

    private final AdjacencyMatrix matrix;

    public MatrixRenderpass(final AdjacencyMatrix matrix) {
      this.matrix = matrix;
      addPass(this);
    }

    private final double SIZE = 10.0;

    @Override
    public void render(final Graphics2D gfx, final KanvasContext ctx) {
      for(int row = 0; row < matrix.size(); ++row) {
        for(int col = 0; col < matrix.size(); ++col) {
          final Rectangle2D rect = new Rectangle2D.Double(col * SIZE, row * SIZE, SIZE,
              SIZE);
          gfx.setColor(matrix.getColorFor(row, col));
          gfx.fill(rect);
          gfx.setColor(Color.BLACK);
          gfx.draw(rect);
        }
      }
    }

    private Position pick(final Point2D pos) {
      final int row;
      final double x = pos.getX();
      if(x < 0 || x > matrix.size() * SIZE) {
        row = -1;
      } else {
        row = (int) (x / SIZE);
      }
      final int col;
      final double y = pos.getY();
      if(y < 0 || y > matrix.size() * SIZE) {
        col = -1;
      } else {
        col = (int) (y / SIZE);
      }
      return new Position(row, col);
    }

    @Override
    public String getTooltip(final Point2D p) {
      final Position pos = pick(p);
      if(pos.row < 0) {
        if(pos.col < 0) return "";
        return matrix.getName(pos.col);
      }
      if(pos.col < 0) return matrix.getName(pos.row);
      return "row: " + matrix.getName(pos.row)
          + " col: " + matrix.getName(pos.col)
          + " value: " + matrix.get(pos.row, pos.col);
    }

    @Override
    public boolean isHUD() {
      return false;
    }

    @Override
    public Rectangle2D getBoundingBox() {
      return new Rectangle2D.Double(0, 0, matrix.size() * SIZE, matrix.size() * SIZE);
    }

  }

  public static void main(final String[] args) {
    final AdjacencyMatrix matrix = new AdjacencyMatrix(100);
    for(int col = 0; col < matrix.size(); ++col) {
      matrix.setName(col, "attr" + col);
      for(int row = 0; row < matrix.size(); ++row) {
        matrix.set(row, col, Math.random());
      }
    }
    final Canvas c = new Canvas(new MatrixRenderpass(matrix), 500, 500);
    c.setMargin(10);
    c.setBackground(Color.WHITE);
    final JFrame frame = new JFrame("Nodelink");
    c.addAction(KeyEvent.VK_Q, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        frame.dispose();
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
