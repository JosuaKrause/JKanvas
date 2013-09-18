package jkanvas.examples;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import jkanvas.Canvas;
import jkanvas.FrameRateDisplayer;
import jkanvas.animation.AnimatedPainter;
import jkanvas.painter.FrameRateHUD;
import jkanvas.painter.SimpleTextHUD;
import jkanvas.painter.TextHUD;
import jkanvas.present.DefaultSlideMetrics;
import jkanvas.present.Presentation;
import jkanvas.present.Slide;
import jkanvas.present.SlideMetrics;
import jkanvas.present.SlideMetrics.VerticalSlideAlignment;
import jkanvas.present.TextRender;
import jkanvas.util.Screenshot;

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
    // let p refresh the Canvas
    p.addRefreshable(c);
    c.setAnimator(p);
    // configure the Canvas
    // c.setMargin(40);
    c.setBackground(Color.WHITE);
    c.setFrameRateDisplayer(new FrameRateHUD());
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
    c.addAction(KeyEvent.VK_F, new AbstractAction() {

      private FrameRateDisplayer frd;

      @Override
      public void actionPerformed(final ActionEvent e) {
        final FrameRateDisplayer tmp = frd;
        frd = c.getFrameRateDisplayer();
        c.setFrameRateDisplayer(tmp);
      }

    });
    final SimpleTextHUD pause = new SimpleTextHUD(TextHUD.LEFT, TextHUD.TOP);
    pause.addLine("paused");
    pause.setVisible(false);
    p.addHUDPass(pause);
    c.addAction(KeyEvent.VK_T, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        final boolean b = !p.isStopped();
        pause.setVisible(b);
        p.setStopped(b);
      }

    });
    c.addAction(KeyEvent.VK_P, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent ae) {
        try {
          final File png = Screenshot.savePNG(new File("pics"), "group", c);
          System.out.println("Saved screenshot in " + png);
        } catch(final IOException e) {
          e.printStackTrace();
        }
      }

    });
    final SimpleTextHUD info = new SimpleTextHUD(TextHUD.RIGHT, TextHUD.BOTTOM);
    c.addAction(KeyEvent.VK_H, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        info.setVisible(!info.isVisible());
        c.refresh();
      }

    });
    info.addLine("T: Pause animation");
    info.addLine("F: Toggle Framerate Display");
    info.addLine("P: Take Photo");
    info.addLine("H: Toggle Help");
    info.addLine("R: Reset View");
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
