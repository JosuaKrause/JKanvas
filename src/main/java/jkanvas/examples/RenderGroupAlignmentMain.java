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
import jkanvas.animation.AnimationAction;
import jkanvas.animation.AnimationTiming;
import jkanvas.matrix.AbstractMutableQuadraticMatrix;
import jkanvas.matrix.CellRealizer;
import jkanvas.matrix.DefaultCellRealizer;
import jkanvas.matrix.MutableQuadraticMatrix;
import jkanvas.matrix.QuadraticMatrix;
import jkanvas.painter.SimpleTextHUD;
import jkanvas.painter.TextHUD;
import jkanvas.painter.groups.LinearGroup;
import jkanvas.painter.groups.LinearGroup.Alignment;
import jkanvas.util.Screenshot;

/**
 * An example showing the render group alignment feature.
 * 
 * @author Manuel Hotz <manuel.hotz@uni-konstanz.de>
 */
public class RenderGroupAlignmentMain extends MatrixMain {

  /**
   * Creates a render group alignment matrix.
   * 
   * @param matrix The matrix.
   * @param cellDrawer The cell realizer.
   * @param manager The update manager.
   */
  public RenderGroupAlignmentMain(final QuadraticMatrix<Double> matrix,
      final CellRealizer<QuadraticMatrix<Double>> cellDrawer,
      final RefreshManager manager) {
    super(matrix, cellDrawer, manager);
  }

  /**
   * Starts the example application.
   * 
   * @param args No arguments.
   */
  public static void main(final String[] args) {
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
    final AnimatedPainter p = new AnimatedPainter();
    // TODO #43 -- Java 8 simplification
    final LinearGroup<RenderGroupAlignmentMain> group =
        new LinearGroup<RenderGroupAlignmentMain>(p, true, 50.0, AnimationTiming.SMOOTH) {

          @Override
          public void draw(final Graphics2D g, final KanvasContext ctx) {
            super.draw(g, ctx);
            g.setColor(Color.GRAY);
            final Rectangle2D rect = new Rectangle2D.Double();
            getBoundingBox(rect);
            g.draw(rect);
          }

        };
    for(int num = 0; num < 3; ++num) {
      // TODO #43 -- Java 8 simplification
      final MutableQuadraticMatrix<Double> matrix = new AbstractMutableQuadraticMatrix<Double>(
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
      // add to group
      final RenderGroupAlignmentMain matrixMain = new RenderGroupAlignmentMain(
          matrix, cellColor, p);
      group.addRenderpass(matrixMain);
    }
    // add group
    p.addPass(group);
    final Canvas c = new Canvas(p, true, 500, 500);
    // TODO #43 -- Java 8 simplification
    group.setOnFinish(new AnimationAction() {

      @Override
      public void animationFinished() {
        final Rectangle2D rect = new Rectangle2D.Double();
        p.getBoundingBox(rect);
        c.setRestriction(rect, AnimationTiming.SMOOTH, null);
        group.setOnFinish(null);
      }

    });
    group.invalidate();
    // let p refresh the Canvas
    p.addRefreshable(c);
    c.setAnimator(p);
    // configure the Canvas
    // c.setMargin(40);
    c.setBackground(Color.WHITE);
    // TODO #43 -- Java 8 simplification
    final JFrame frame = new JFrame("Matrix") {

      @Override
      public void dispose() {
        c.dispose();
        super.dispose();
      }

    };
    // add actions to the Canvas
    // TODO #43 -- Java 8 simplification
    c.addAction(KeyEvent.VK_Q, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        frame.dispose();
      }

    });
    // TODO #43 -- Java 8 simplification
    c.addAction(KeyEvent.VK_R, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        c.reset();
      }

    });
    // TODO #43 -- Java 8 simplification
    c.addAction(KeyEvent.VK_P, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent ae) {
        try {
          final File png = Screenshot.save(new File("pics"), "group", c);
          System.out.println("Saved screenshot in " + png);
        } catch(final IOException e) {
          e.printStackTrace();
        }
      }

    });
    // TODO #43 -- Java 8 simplification
    c.addAction(KeyEvent.VK_C, new AbstractAction() {

      private final Alignment[] align =
      { Alignment.LEFT, Alignment.RIGHT, Alignment.CENTER};

      private int ac = 0;

      @Override
      public void actionPerformed(final ActionEvent e) {
        group.setAlignment(align[ac++]);
        if(ac >= align.length) {
          ac = 0;
        }
      }

    });
    final SimpleTextHUD info = new SimpleTextHUD(TextHUD.RIGHT, TextHUD.BOTTOM);
    // TODO #43 -- Java 8 simplification
    c.addAction(KeyEvent.VK_H, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        info.setVisible(!info.isVisible());
        c.refresh();
      }

    });
    info.addLine("P: Take Photo");
    info.addLine("H: Toggle Help");
    info.addLine("R: Reset View");
    info.addLine("C: Change alignment");
    info.addLine("Q/ESC: Quit");
    p.addHUDPass(info);
    // pack and show window
    frame.add(c);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    c.reset();
    frame.setVisible(true);
  }

}
