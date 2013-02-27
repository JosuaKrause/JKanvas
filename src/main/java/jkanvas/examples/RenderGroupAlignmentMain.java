package jkanvas.examples;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import jkanvas.Canvas;
import jkanvas.KanvasContext;
import jkanvas.RefreshManager;
import jkanvas.animation.AnimatedPainter;
import jkanvas.animation.AnimationTiming;
import jkanvas.groups.LinearGroup;
import jkanvas.groups.LinearGroup.Alignment;
import jkanvas.matrix.AbstractQuadraticMatrix;
import jkanvas.matrix.CellRealizer;
import jkanvas.matrix.MutableQuadraticMatrix;
import jkanvas.matrix.QuadraticMatrix;
import jkanvas.util.Screenshot;

/**
 * An example showing the render group alignment feature.
 * 
 * @author Manuel Hotz <manuel.hotz@uni-konstanz.de>
 */
public class RenderGroupAlignmentMain extends MatrixMain {

  /**
   * @param matrix The matrix
   * @param cellDrawer The cell realizer
   * @param manager The update manager
   */
  public RenderGroupAlignmentMain(final QuadraticMatrix<Double> matrix,
      final CellRealizer<QuadraticMatrix<Double>> cellDrawer, final RefreshManager manager) {
    super(matrix, cellDrawer, manager);
  }

  /**
   * Starts the example application.
   * 
   * @param args No arguments.
   */
  public static void main(final String[] args) {

    final CellRealizer<QuadraticMatrix<Double>> cellColor = new CellRealizer<QuadraticMatrix<Double>>() {

      @Override
      public void drawCell(final Graphics2D g, final KanvasContext ctx,
          final Rectangle2D rect, final QuadraticMatrix<Double> matrix, final int row,
          final int col, final boolean isSelected, final boolean hasSelection) {
        final Double val = matrix.get(row, col);
        g.setColor(getColor(val, hasSelection && isSelected));
        g.fill(rect);
        g.setColor(Color.BLACK);
        g.draw(rect);
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
    final AnimatedPainter p = new AnimatedPainter();

    final LinearGroup group = new LinearGroup(p, true, 50.0, AnimationTiming.SMOOTH) {
      @Override
      public void draw(final Graphics2D gfx, final KanvasContext ctx) {
        super.draw(gfx, ctx);
        gfx.setColor(Color.GRAY);
        gfx.draw(getBoundingBox());
      }
    };

    for(int num = 0; num < 3; ++num) {

      final MutableQuadraticMatrix<Double> matrix = new AbstractQuadraticMatrix<Double>(
          9 + num * num) {

        @Override
        protected Double[][] createMatrix(final int size) {
          return new Double[size][size];
        }

      };

      // set names, widths, and heights of rows / columns
      for(int i = 0; i < matrix.size(); ++i) {
        matrix.setName(i, "Attr" + i);
        matrix.setWidth(i, 20);
        matrix.setHeight(i, 20);
      }

      // fill the matrix with random values
      for(int col = 0; col < matrix.size(); ++col) {
        for(int row = 0; row < matrix.size(); ++row) {
          matrix.set(row, col, Math.random());
        }
      }

      final RenderGroupAlignmentMain matrixMain = new RenderGroupAlignmentMain(matrix,
          cellColor, p);
      group.addRenderpass(matrixMain);
    }

    p.addPass(group);

    final Canvas c = new Canvas(p, true, 500, 500);

    // let RefreshManager refresh the Canvas
    p.addRefreshable(c);
    // configure the Canvas
    // c.setMargin(40);
    c.setBackground(Color.WHITE);
    final JFrame frame = new JFrame("Matrix") {

      @Override
      public void dispose() {
        c.dispose();
        super.dispose();
      }

    };
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
    c.addAction(KeyEvent.VK_C, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        if(group.getAlignment().equals(Alignment.LEFT)) {
          group.setAlignment(Alignment.MIDDLE);
        } else if(group.getAlignment().equals(Alignment.MIDDLE)) {
          group.setAlignment(Alignment.RIGHT);
        } else {
          group.setAlignment(Alignment.LEFT);
        }
      }

    });
    // pack and show window
    frame.add(c);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    c.setRestriction(p.getBoundingBox());
    c.reset();
    frame.setVisible(true);
  }

}
