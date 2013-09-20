package jkanvas.examples;

import java.awt.event.KeyEvent;

import jkanvas.Canvas;
import jkanvas.animation.AnimatedPainter;
import jkanvas.animation.AnimationTiming;
import jkanvas.painter.SimpleTextHUD;
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
    final Canvas c = new Canvas(p, true, 1024, 768);
    final SlideMetrics m = new DefaultSlideMetrics();
    final SimpleTextHUD info = ExampleUtil.setupCanvas("Presentation", c, p,
        true, true, true);

    final Presentation present = new Presentation(c, m, AnimationTiming.SMOOTH);
    present.setIds("presentation");
    // slide 0
    final Slide s0 = new Slide();
    present.addRenderpass(s0);
    s0.add(new TextRender("Hello World!", VerticalSlideAlignment.TOP));
    // slide 1
    final Slide s1 = new Slide();
    s1.add(new TextRender("Hello World! 0", VerticalSlideAlignment.TOP));
    present.addRenderpass(s1);
    s1.add(new TextRender(TextRender.BULLET + " Jj", VerticalSlideAlignment.TOP, 1));
    s1.add(new TextRender("The quick brown fox jumped over the lazy dog!",
        VerticalSlideAlignment.TOP));
    s1.add(new TextRender("Hello World! 3", VerticalSlideAlignment.TOP));
    // slide 2
    final Slide s2 = new Slide();
    s2.add(new TextRender("The quick brown fox jumped over the lazy dog!",
        VerticalSlideAlignment.TOP));
    s2.add(new TextRender("hi! 0", VerticalSlideAlignment.TOP));
    s2.add(new TextRender(TextRender.BULLET + " Jj", VerticalSlideAlignment.TOP, 1));
    present.addRenderpass(s2);

    p.addPass(present);
    c.addMessageAction(KeyEvent.VK_LEFT, "presentation#slide:prev");
    c.addMessageAction(KeyEvent.VK_RIGHT, "presentation#slide:next");
    c.addMessageAction(KeyEvent.VK_S, "presentation#present:toggle");
    info.addLine("S: Toggle presentation mode");
    info.addLine("LEFT: Previous slide");
    info.addLine("RIGHT: Next slide");
    present.setPresentationMode(true);
  }

}
