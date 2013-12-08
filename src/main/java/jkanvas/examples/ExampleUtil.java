package jkanvas.examples;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.io.File;
import java.io.IOException;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.WindowConstants;

import jkanvas.Canvas;
import jkanvas.FrameRateDisplayer;
import jkanvas.animation.AnimatedPainter;
import jkanvas.animation.AnimationAction;
import jkanvas.painter.FrameRateHUD;
import jkanvas.painter.RenderpassPainter;
import jkanvas.painter.SimpleTextHUD;
import jkanvas.painter.TextHUD;
import jkanvas.util.Screenshot;

/**
 * Common tasks for example applications.
 * 
 * @author Joschi <josua.krause@googlemail.com>
 */
public final class ExampleUtil {

  /** Nothing to create. */
  private ExampleUtil() {
    throw new AssertionError();
  }

  /**
   * Sets up the canvas with standard actions.
   * 
   * @param name The name of the frame.
   * @param c The canvas.
   * @param p The render pass painter.
   * @param fps Whether to give the option to show fps.
   * @param pause Whether to give the option to pause animations. This is only
   *          possible when the render pass painter can animation.
   * @param reset Whether to give the option to reset the view.
   * @param frameScreenshot Whether to take screenshots from the frame or the
   *          canvas.
   * @return The info HUD to enable more actions.
   */
  public static SimpleTextHUD setupCanvas(final String name, final Canvas c,
      final RenderpassPainter p, final boolean fps,
      final boolean pause, final boolean reset, final boolean frameScreenshot) {
    final JFrame frame = new JFrame(name) {

      @Override
      public void dispose() {
        c.dispose();
        super.dispose();
      }

    };
    return setupCanvas(frame, c, p, fps, pause, reset, frameScreenshot);
  }

  /**
   * Sets up the canvas with standard actions.
   * 
   * @param frame The frame for the canvas.
   * @param c The canvas.
   * @param p The render pass painter.
   * @param fps Whether to give the option to show fps.
   * @param pause Whether to give the option to pause animations. This is only
   *          possible when the render pass painter can animation.
   * @param reset Whether to give the option to reset the view.
   * @param frameScreenshot Whether to take screenshots from the frame or the
   *          canvas.
   * @return The info HUD to enable more actions.
   */
  public static SimpleTextHUD setupCanvas(final JFrame frame, final Canvas c,
      final RenderpassPainter p, final boolean fps,
      final boolean pause, final boolean reset, final boolean frameScreenshot) {
    Objects.requireNonNull(frame);
    c.setBackground(Color.WHITE);
    if(p instanceof AnimatedPainter) {
      final AnimatedPainter ap = (AnimatedPainter) p;
      ap.addRefreshable(c);
      c.setAnimator(ap);
    }
    if(fps) {
      c.setFrameRateDisplayer(new FrameRateHUD());
    }
    // add actions
    c.addAction(KeyEvent.VK_Q, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        frame.dispose();
      }

    });
    if(fps) {
      c.addAction(KeyEvent.VK_F, new AbstractAction() {

        private FrameRateDisplayer frd;

        @Override
        public void actionPerformed(final ActionEvent e) {
          final FrameRateDisplayer tmp = frd;
          frd = c.getFrameRateDisplayer();
          c.setFrameRateDisplayer(tmp);
        }

      });
    }
    final AnimatedPainter ap;
    if(p instanceof AnimatedPainter) {
      ap = (AnimatedPainter) p;
    } else {
      ap = null;
    }
    if(pause) {
      final SimpleTextHUD pauseHUD = new SimpleTextHUD(TextHUD.LEFT, TextHUD.TOP);
      if(ap == null) throw new AssertionError("Need animated painter to stop animation!");
      pauseHUD.addLine("paused");
      pauseHUD.setVisible(false);
      p.addHUDPass(pauseHUD);
      c.addAction(KeyEvent.VK_T, new AbstractAction() {

        @Override
        public void actionPerformed(final ActionEvent e) {
          final boolean b = !ap.isStopped();
          pauseHUD.setVisible(b);
          ap.setStopped(b);
        }

      });
    }
    c.addAction(KeyEvent.VK_P, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent ae) {
        try {
          final boolean before;
          if(ap != null) {
            before = ap.isStopped();
            ap.setStopped(true);
          } else {
            before = false;
          }
          final File png = Screenshot.savePNG(new File("pics"), "nodelink",
              frameScreenshot ? frame.getRootPane() : c);
          System.out.println("Saved screenshot in " + png);
          if(ap != null) {
            ap.setStopped(before);
          }
        } catch(final IOException e) {
          e.printStackTrace();
        }
      }

    });
    if(reset) {
      c.addAction(KeyEvent.VK_R, new AbstractAction() {

        @Override
        public void actionPerformed(final ActionEvent e) {
          c.reset();
        }

      });
    }
    final SimpleTextHUD info = new SimpleTextHUD(TextHUD.RIGHT, TextHUD.BOTTOM);
    info.setIds("info");
    if(pause) {
      info.addLine("T: Pause animation");
    }
    if(fps) {
      info.addLine("F: Toggle Framerate Display");
    }
    if(reset) {
      info.addLine("R: Reset View");
    }
    info.addLine("P: Take Photo");
    info.addLine("H: Toggle Help");
    info.addLine("Q/ESC: Quit");
    p.addHUDPass(info);
    c.addMessageAction(KeyEvent.VK_H, "info#visible:toggle");
    // pack and show window
    frame.add(c);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
    if(reset) {
      c.scheduleAction(new AnimationAction() {

        @Override
        public void animationFinished() {
          c.reset();
        }

      }, 0);
    }
    return info;
  }

}
