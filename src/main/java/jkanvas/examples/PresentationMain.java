package jkanvas.examples;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import jkanvas.Canvas;
import jkanvas.animation.AnimatedPainter;
import jkanvas.json.JSONElement;
import jkanvas.json.JSONReader;
import jkanvas.painter.SimpleTextHUD;
import jkanvas.present.DefaultSlideMetrics;
import jkanvas.present.Presentation;
import jkanvas.present.SlideMetrics;
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
    present.setPresentationMode(true);
  }

}
