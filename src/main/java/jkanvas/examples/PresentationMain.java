package jkanvas.examples;

import jkanvas.Canvas;
import jkanvas.animation.AnimatedPainter;
import jkanvas.present.DefaultSlideMetrics;
import jkanvas.present.Presentation;
import jkanvas.present.Slide;
import jkanvas.present.SlideMetrics;
import jkanvas.present.SlideMetrics.VerticalSlideAlignment;
import jkanvas.present.TextRender;

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
   */
  public static void main(final String[] args) {
    final AnimatedPainter p = new AnimatedPainter();
    final SlideMetrics m = new DefaultSlideMetrics();
    final Presentation present = new Presentation(p, m);
    final Slide s0 = new Slide();
    present.addRenderpass(s0);
    s0.add(new TextRender("Hello World!", VerticalSlideAlignment.TOP));
    final Slide s1 = new Slide();
    s1.add(new TextRender("Hello World! 0", VerticalSlideAlignment.TOP));
    present.addRenderpass(s1);
    s1.add(new TextRender(TextRender.BULLET + " Jj", VerticalSlideAlignment.TOP, 1));
    s1.add(new TextRender("The quick brown fox jumped over the lazy dog!",
        VerticalSlideAlignment.TOP));
    s1.add(new TextRender("Hello World! 3", VerticalSlideAlignment.TOP));
    p.addPass(present);
    final Canvas c = new Canvas(p, true, 1024, 768);
    ExampleUtil.setupCanvas("Presentation", c, p, true, true, true);
  }

}
