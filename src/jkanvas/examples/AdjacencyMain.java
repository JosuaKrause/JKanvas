package jkanvas.examples;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.HashSet;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import jkanvas.Canvas;
import jkanvas.KanvasPainter;
import jkanvas.adjacency.AdjacencyMatrix;
import jkanvas.adjacency.CellColor;
import jkanvas.adjacency.MatrixPainter;
import jkanvas.adjacency.MatrixPosition;

public class AdjacencyMain {

  private static class DoubleMatrix implements AdjacencyMatrix<Double> {

    private final String[] names;

    private final double[][] matrix;

    private final double[] widths;

    private final double[] heights;

    public DoubleMatrix(final int size) {
      names = new String[size];
      widths = new double[size];
      heights = new double[size];
      matrix = new double[size][];
      for(int i = 0; i < size; ++i) {
        matrix[i] = new double[size];
      }
    }

    @Override
    public double getHeight(final int row) {
      return heights[row];
    }

    @Override
    public double getWidth(final int col) {
      return heights[col];
    }

    @Override
    public void setHeight(final int row, final double value) {
      heights[row] = value;
    }

    @Override
    public void setWidth(final int col, final double value) {
      heights[col] = value;
    }

    @Override
    public String getName(final int row) {
      return names[row] == null ? "" : names[row];
    }

    @Override
    public void setName(final int row, final String name) {
      names[row] = name;
    }

    @Override
    public Double get(final int row, final int col) {
      return matrix[row][col];
    }

    @Override
    public void set(final int row, final int col, final Double value) {
      matrix[row][col] = value;
    }

    @Override
    public int size() {
      return names.length;
    }

  }

  public static void main(final String[] args) {
    final AdjacencyMatrix<Double> matrix = new DoubleMatrix(100);
    for(int col = 0; col < matrix.size(); ++col) {
      matrix.setName(col, "attr" + col);
      for(int row = 0; row < matrix.size(); ++row) {
        matrix.set(row, col, Math.random());
      }
    }
    for(int i = 0; i < matrix.size(); ++i) {
      matrix.setWidth(i, 20 + Math.random() * 80);
      matrix.setHeight(i, 20 + Math.random() * 80);
    }
    final CellColor<Double> cellColor = new CellColor<Double>() {

      @Override
      public Color getColorFor(final int row, final int col,
          final Double value, final boolean isSelected) {
        final double v = value - 0.5;
        final double hue = v > 0 ? 0 : 180.0 / 360.0;
        final double rv = Math.abs(v) * 2;
        return Color.getHSBColor((float) hue, (float) rv, isSelected ? 1f : .8f);
      }

      @Override
      public Color getBorderColorFor(final int row, final int col,
          final Double value, final boolean isSelected) {
        return Color.BLACK;
      }

    };
    final KanvasPainter p = new MatrixPainter<Double>(matrix, cellColor) {

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
        final MatrixPosition pos = pick(p);
        if(pos == null) return "";
        return "row: " + matrix.getName(pos.row)
            + " col: " + matrix.getName(pos.col)
            + " value: " + matrix.get(pos.row, pos.col);
      }

    };
    final Canvas c = new Canvas(p, 500, 500);
    c.setMargin(10);
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
    frame.add(c);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    c.reset();
    frame.setVisible(true);
  }

}
