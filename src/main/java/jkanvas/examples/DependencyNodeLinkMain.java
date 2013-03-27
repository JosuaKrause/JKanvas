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
import jkanvas.nodelink.DependencyNodeLinkRenderpass;
import jkanvas.nodelink.DependencyNodeLinkView;
import jkanvas.painter.FrameRateHUD;
import jkanvas.painter.SimpleTextHUD;
import jkanvas.painter.TextHUD;
import jkanvas.util.Screenshot;

/**
 * A small example showing dependencies between objects.
 * 
 * @author Joschi <josua.krause@gmail.com>
 */
public final class DependencyNodeLinkMain {

  /**
   * Creates a window with the node-link diagram.
   * 
   * @param start The start object or <code>null</code> if the own canvas should
   *          be used.
   * @param pkgs The allowed packages.
   */
  public static void createFrame(final Object start, final String[] pkgs) {
    final int w = 800;
    final int h = 600;
    // configure Canvas
    final AnimatedPainter p = new AnimatedPainter();
    final Canvas c = new Canvas(p, w, h);
    c.setBackground(Color.WHITE);
    c.setFrameRateDisplayer(new FrameRateHUD());
    final DependencyNodeLinkRenderpass pass =
        new DependencyNodeLinkRenderpass(start == null ? c : start, pkgs);
    p.addRefreshable(c);
    c.setAnimator(p);
    p.addPass(pass);
    final JFrame frame = new JFrame("Node-Link") {

      @Override
      public void dispose() {
        c.dispose();
        super.dispose();
      }

    };
    // add actions
    c.addAction(KeyEvent.VK_Q, new AbstractAction() {

      @Override
      public void actionPerformed(final ActionEvent e) {
        frame.dispose();
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
          final File png = Screenshot.savePNG(new File("pics"), "nodelink", c);
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
    info.addLine("R: Lay out nodes in a circle");
    info.addLine("Q/ESC: Quit");
    p.addHUDPass(info);
    // pack and show window
    frame.add(c);
    frame.pack();
    frame.setLocationRelativeTo(null);
    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
    frame.setVisible(true);
  }

  /**
   * Starts a small example.
   * 
   * @param args The ignored arguments.
   */
  public static void main(final String[] args) {
    // Canvas.DEBUG_BBOX = true;
    createFrame(null, DependencyNodeLinkView.STD_CLASSES);
  }

}
