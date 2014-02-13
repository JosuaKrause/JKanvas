package jkanvas.examples;

import java.io.IOException;

import jkanvas.Canvas;
import jkanvas.CanvasSetup;
import jkanvas.animation.AnimatedPainter;
import jkanvas.io.json.JSONElement;
import jkanvas.io.json.JSONReader;
import jkanvas.painter.SimpleTextHUD;
import jkanvas.present.DefaultSlideMetrics;
import jkanvas.present.Presentation;
import jkanvas.present.SlideMetrics;
import jkanvas.util.Resource;

/**
 * A short example showing the presentation capabilities of Kanvas.
 * 
 * @author Joschi <josua.krause@gmail.com>
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
    final SimpleTextHUD info = CanvasSetup.setupCanvas("Presentation", c, p,
        true, true, true, false);

    final JSONElement el = new JSONReader(new Resource("test.json").reader()).get();
    final SlideMetrics m = new DefaultSlideMetrics();

    final Presentation present = Presentation.fromJSON(c, info, el, m);
    p.addPass(present);
    present.setPresentationMode(true);
  }

}
