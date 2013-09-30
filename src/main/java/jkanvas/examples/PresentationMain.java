package jkanvas.examples;

import java.awt.Color;
import java.awt.Graphics2D;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import jkanvas.Canvas;
import jkanvas.KanvasContext;
import jkanvas.KanvasPainter;
import jkanvas.ViewConfiguration;
import jkanvas.animation.AnimatedPainter;
import jkanvas.animation.AnimationTiming;
import jkanvas.groups.LinearGroup;
import jkanvas.json.JSONElement;
import jkanvas.json.JSONReader;
import jkanvas.matrix.AbstractQuadraticMatrix;
import jkanvas.matrix.CellRealizer;
import jkanvas.matrix.DefaultCellRealizer;
import jkanvas.matrix.MutableQuadraticMatrix;
import jkanvas.matrix.QuadraticMatrix;
import jkanvas.painter.SimpleTextHUD;
import jkanvas.present.DefaultSlideMetrics;
import jkanvas.present.PortalRender;
import jkanvas.present.Presentation;
import jkanvas.present.SlideMetrics;
import jkanvas.present.SlideMetrics.HorizontalSlideAlignment;
import jkanvas.present.SlideMetrics.VerticalSlideAlignment;
import jkanvas.util.ResourceLoader;

/**
 * A short example showing the presentation capabilities of Kanvas.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class PresentationMain {

  /**
   * Starts the example application.
   * 
   * @param args No arguments.
   * @throws IOException I/O Exception.
   */
  public static void main(final String[] args) throws IOException {
    final AnimatedPainter p = new AnimatedPainter();
    final Canvas c = new Canvas(p, true, 1024, 768);
    final SimpleTextHUD info = ExampleUtil.setupCanvas("Presentation", c, p,
        true, true, true);

    final InputStream json = ResourceLoader.getResourceLoader().loadResource("test.json");
    final JSONElement el = new JSONReader(new InputStreamReader(json, "UTF-8")).get();
    final SlideMetrics m = new DefaultSlideMetrics();

    final Presentation present = Presentation.fromJSON(c, info, el, m);
    p.addPass(present);
    final ViewConfiguration cfg = new ViewConfiguration(c, matrix(), true);
    present.addArea(new PortalRender(
        present.getRenderpass(present.renderpassCount() - 1), c.getViewConfiguration(),
        cfg, HorizontalSlideAlignment.CENTER, VerticalSlideAlignment.CENTER, 200, 200));
    present.setPresentationMode(true);
  }

  private static KanvasPainter matrix() {
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
    final LinearGroup<RenderGroupAlignmentMain> group =
        new LinearGroup<RenderGroupAlignmentMain>(p, true, 50.0, AnimationTiming.SMOOTH) {

          @Override
          public void draw(final Graphics2D g, final KanvasContext ctx) {
            super.draw(g, ctx);
            g.setColor(Color.GRAY);
            g.draw(getBoundingBox());
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
      // add to group
      final RenderGroupAlignmentMain matrixMain = new RenderGroupAlignmentMain(
          matrix, cellColor, p);
      group.addRenderpass(matrixMain);
    }
    // add group
    p.addPass(group);
    group.invalidate();
    return p;
  }

}
