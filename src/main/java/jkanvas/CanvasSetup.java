package jkanvas;

import java.awt.Color;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.util.Objects;

import javax.swing.JFrame;
import javax.swing.WindowConstants;

import jkanvas.animation.AnimatedPainter;
import jkanvas.painter.RenderpassPainter;
import jkanvas.painter.SimpleTextHUD;
import jkanvas.painter.TextHUD;

/**
 * Setting up a canvas.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public final class CanvasSetup {

  /** Nothing to create. */
  private CanvasSetup() {
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
    final JFrame frame = new JFrame(name);
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
      final RenderpassPainter p, final boolean fps, final boolean pause,
      final boolean reset, final boolean frameScreenshot) {
    return setupCanvas(frame, c, p, fps, pause, reset, frameScreenshot, true);
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
   * @param layout Whether to add the canvas and lay out the component and frame
   *          afterwards.
   * @return The info HUD to enable more actions.
   */
  public static SimpleTextHUD setupCanvas(final JFrame frame, final Canvas c,
      final RenderpassPainter p, final boolean fps, final boolean pause,
      final boolean reset, final boolean frameScreenshot, final boolean layout) {
    Objects.requireNonNull(frame);
    c.setBackground(Color.WHITE);
    if(p instanceof AnimatedPainter) {
      final AnimatedPainter ap = (AnimatedPainter) p;
      ap.addRefreshable(c);
      c.setAnimator(ap);
    }
    final SimpleTextHUD info = new SimpleTextHUD(TextHUD.RIGHT, TextHUD.BOTTOM);
    final CanvasMessageHandler cmh = new DefaultMessageHandler(frame);
    final String canvasId = cmh.getCanvasIds().trim();
    c.setMessageHandler(cmh);
    // add actions
    c.addMessageAction(KeyEvent.VK_Q, canvasId + "#quit");
    info.addLine("Q/ESC: Quit");
    if(fps) {
      c.addMessageAction(KeyEvent.VK_F, canvasId + "#fps:toggle");
      info.addLine("F: Toggle Framerate Display");
    }
    if(pause) {
      c.addMessageAction(KeyEvent.VK_T, canvasId + "#pause:toggle");
      info.addLine("T: Pause animation");
    }
    c.addMessageAction(KeyEvent.VK_P, canvasId + "#photo"
        + (frameScreenshot ? ":window" : ""));
    info.addLine("P: Take Photo");
    if(reset) {
      c.addMessageAction(KeyEvent.VK_R, canvasId + "#reset");
      info.addLine("R: Reset View");
    }
    info.setIds("info");
    c.addMessageAction(KeyEvent.VK_H, "info#visible:toggle");
    info.addLine("H: Toggle Help");
    p.addHUDPass(info);
    if(layout) {
      // pack and show window
      frame.add(c);
      frame.pack();
      frame.setLocationRelativeTo(null);
      frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
      frame.setVisible(true);
    }
    final WindowAdapter wnd = Canvas.getWindowAdapter(frame, c);
    frame.addWindowListener(wnd);
    frame.addWindowStateListener(wnd);
    if(reset) {
      c.postMessage(canvasId + "#reset", 0);
    }
    return info;
  }

}
